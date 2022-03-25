package com.ashish.realtimecalltranscription;

import static com.ashish.realtimecalltranscription.Constants.SpeechRegion;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RecorderService extends Service {

    private static final String TAG = "RecorderService";
    public static String CALL_ID, prevState = TelephonyManager.EXTRA_STATE_IDLE;;
    private static final int SERVICE_ID = 123;
    private boolean hasRecordingStarted = false, shouldCapture = true;
    private CallReceiver callReceiver;
    private Context mContext;
    private CustomAudioStream customAudioStream;
    private SpeechConfig speechConfig;
    private SpeechRecognizer speechRecognizer = null;
    private final Vector<String> convertedTextList = new Vector<>();
    private boolean continuousListeningStarted = false;
    private static final ExecutorService s_executorService;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");
        unregisterReceiver(this.callReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Started service...");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_OUT);
        filter.addAction(Constants.ACTION_IN);
        this.callReceiver = new CallReceiver();
        this.registerReceiver(this.callReceiver, filter);
        mContext = this;
        return START_NOT_STICKY;
    }

    /**
     * Broadcast Receiver to use incoming voice calls for transcription.
     * Refer https://stackoverflow.com/questions/18887636/how-to-record-phone-calls-in-android
     * */
    public class CallReceiver extends BroadcastReceiver {
        Bundle bundle;
        String state;
        public boolean wasRinging = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_IN)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        wasRinging = true;
                        Log.d(TAG, "onReceive: Incoming call");
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging) {
                            init();
                            Log.d(TAG, "onReceive: Received Call");
                            initContinuousRecognition();
                            startForeground(SERVICE_ID, new NotificationCompat.Builder(context, Constants.CHANNEL_ID).build());
                        }
                    } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        if (hasRecordingStarted) {
                            hasRecordingStarted = false;
                            shouldCapture = false;
                            initContinuousRecognition();
                            stopForeground(true);
                            Log.d(TAG, "onReceive: Stopped transcription.");
                        }
                    }
                    prevState = state;
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void init() {
        shouldCapture = true;
        hasRecordingStarted = true;
        initRecognizer();
        createNotificationChannel();
    }

    /**
     * Creates channel to display notification when call is active.
     * */
    private void createNotificationChannel() {
        NotificationChannel notificationChannel = null;
        notificationChannel = new NotificationChannel(Constants.CHANNEL_ID,
                "My Recording channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager mgr = getSystemService(NotificationManager.class);
        mgr.createNotificationChannel(notificationChannel);
    }


    /**
     * Creates the audio stream (VOICE_RECOGNITION)
     * */
    private CustomAudioStream createCustomAudioStream() {
        if (customAudioStream != null) {
            customAudioStream.close();
            customAudioStream = null;
        }

        customAudioStream = new CustomAudioStream();
        return customAudioStream;
    }

    /**
     * Sets up the azure speech SDK. Logs are saved to phonestorage/android/com.realtimecalltranscription/logs.txt
     * */
    private void initRecognizer() {
        Log.d(TAG, "initRecognizer: Prepping up");
        try {
            speechConfig = SpeechConfig.fromSubscription(Constants.SpeechSubscriptionKey, SpeechRegion);
            speechConfig.setSpeechRecognitionLanguage("hi-IN");
            File [] dir = mContext.getExternalFilesDirs(null);
            File logFile = new File(dir[0], "logs.txt");
            speechConfig.setProperty(PropertyId.Speech_LogFilename, logFile.getAbsolutePath());
        } catch (Exception ex) {
            Log.e(TAG, "initRecognizer: " + ex.getMessage());
            return;
        }
    }

    private void initContinuousRecognition() {
        if (continuousListeningStarted) {
            if (speechRecognizer != null) {
                final Future<Void> task = speechRecognizer.stopContinuousRecognitionAsync();
                setOnTaskCompletedListener(task, result -> {
                    Log.i(TAG, "Continuous recognition stopped.");
                    continuousListeningStarted = false;
                });
            } else {
                continuousListeningStarted = false;
            }
            return;
        }

        try {
            convertedTextList.clear();
            AudioConfig audioInput = AudioConfig.fromStreamInput(createCustomAudioStream());
            speechRecognizer = new SpeechRecognizer(speechConfig, audioInput);
            Log.d(TAG, "initContinuousRecognition: init speech recognizer done");

            speechRecognizer.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                final String s = speechRecognitionResultEventArgs.getResult().getText();
                Log.i(TAG, getTimeStamp()+ " Final result received: " + s);
                if(!TextUtils.isEmpty(s)) {
                    convertedTextList.add(s);
                }
            });

            final Future<Void> task = speechRecognizer.startContinuousRecognitionAsync();
            Log.d(TAG, "initContinuousRecognition: Started recognition task..");
            setOnTaskCompletedListener(task, result -> {
                continuousListeningStarted = true;
            });
        } catch (Exception ex) {
            Log.e(TAG, "initContinuousRecognition: " + ex.getMessage());
        }
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("HH.mm.ss").format(new Date());
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    static {
        s_executorService = Executors.newCachedThreadPool();
    }
}

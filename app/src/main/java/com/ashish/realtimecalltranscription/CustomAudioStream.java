package com.ashish.realtimecalltranscription;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;

public class CustomAudioStream extends PullAudioInputStreamCallback {
    private final static int SAMPLE_RATE = 16000;
    private final AudioStreamFormat format;
    private AudioRecord recorder;
    private static final String TAG = "CustomAudioStream";

    public CustomAudioStream() {
        Log.d(TAG, "CustomAudioStream: Created object");
        this.format = AudioStreamFormat.getWaveFormatPCM(SAMPLE_RATE, (short)16, (short)1);
        this.initMic();
    }

    public AudioStreamFormat getFormat() {
        return this.format;
    }

    @Override
    public int read(byte[] bytes) {
        long ret = this.recorder.read(bytes, 0, bytes.length);
        return (int)ret;
    }

    @Override
    public void close() {
        this.recorder.release();
        this.recorder = null;
    }

    @SuppressLint("MissingPermission")
    private void initMic() {
        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build();

        this.recorder = new AudioRecord.Builder()
                .setAudioFormat(audioFormat)
                .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                .setBufferSizeInBytes(2048)
                .build();
        Log.d(TAG, "initMic: Started recording using AudioRecord for cont recognition");

        this.recorder.startRecording();
    }
}

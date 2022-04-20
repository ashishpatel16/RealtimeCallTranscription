## Sample Android Application for Azure Speech SDK troubleshooting

* Clone this repo and update `Constants.SpeechSubscriptionKey` with your subscription key.
* Build the APK and install on android 10/11.
* Allow permissions, allow it to be the device admin app and enable accessibility service.
* Get an incoming call and check logs under the tag `RecorderService`
* Note : Uninstalling the app requires the user to **manually uncheck the app from device admin app list**.

## Problem Faced
* We have been unable to transcribe **incoming call audio** on android 10/11. However, it works perfectly on older versions. We have used AudioRecord instance for the audio source as suggested by the sdkdemo sample github repository (https://github.com/Azure-Samples/cognitive-services-speech-sdk/tree/master/samples/java/android/sdkdemo). 

## Below are some logs for a recent call - Not working on android 11

* Between **13.52.09** and **13.52.45** audio processing wasn't achieved even after enabling the loudspeaker on intended device.
* **logs.txt** shows the entire stack trace when tested on android 11 - Unable to recognize audio.
````
2022-03-25 13:51:18.558 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: onStartCommand: Started service...
2022-03-25 13:51:52.825 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: onReceive: Incoming call
2022-03-25 13:51:55.044 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: initRecognizer: Prepping up
2022-03-25 13:51:55.140 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: onReceive: Received Call
2022-03-25 13:51:55.316 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: initContinuousRecognition: init speech recognizer done
2022-03-25 13:51:55.318 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: initContinuousRecognition: Started recognition task..
2022-03-25 13:52:01.473 23340-23603/com.ashish.realtimecalltranscription I/RecorderService: 13.52.01 Final result received: हैलो, मैंने फ़ोन उठाया।
2022-03-25 13:52:09.193 23340-23603/com.ashish.realtimecalltranscription I/RecorderService: 13.52.09 Final result received: टिके में दूसरे दूर से बात करता हूँ, दूसरे एंड से।
2022-03-25 13:52:45.444 23340-23603/com.ashish.realtimecalltranscription I/RecorderService: 13.52.45 Final result received:
2022-03-25 13:52:51.457 23340-23340/com.ashish.realtimecalltranscription D/RecorderService: onReceive: Stopped transcription.
2022-03-25 13:52:52.220 23340-23603/com.ashish.realtimecalltranscription I/RecorderService: 13.52.52 Final result received:
2022-03-25 13:52:52.318 23340-23607/com.ashish.realtimecalltranscription I/RecorderService: Continuous recognition stopped. 
`````

## Below are some logs for a recent call - Working on android 8.1 (Expected output)
* Between **15:49:11** and **15:49:51** audio processing wasn properly achieved on intended device.
* **logs-working-on-android-8.1.txt** shows the entire stack trace when tested on android 8.1 - Working ideally as expected.
`````
2022-04-20 15:48:57.202 30358-30358/com.ashish.realtimecalltranscription D/RecorderService: onReceive: Incoming call
2022-04-20 15:49:04.637 30358-30358/com.ashish.realtimecalltranscription D/RecorderService: initRecognizer: Prepping up
2022-04-20 15:49:04.760 30358-30358/com.ashish.realtimecalltranscription D/RecorderService: onReceive: Received Call
2022-04-20 15:49:04.866 30358-30358/com.ashish.realtimecalltranscription D/RecorderService: initContinuousRecognition: init speech recognizer done
2022-04-20 15:49:04.872 30358-30358/com.ashish.realtimecalltranscription D/RecorderService: initContinuousRecognition: Started recognition task..
2022-04-20 15:49:11.864 30358-31768/com.ashish.realtimecalltranscription I/RecorderService: 15.49.11 Final result received: मेरी आवाज़ आ रही है।
2022-04-20 15:49:28.948 30358-31768/com.ashish.realtimecalltranscription I/RecorderService: 15.49.28 Final result received: से बात कर रहा हूँ।
2022-04-20 15:49:40.941 30358-31768/com.ashish.realtimecalltranscription I/RecorderService: 15.49.40 Final result received: हैलो, मैं अपने फ़ोन से बात कर रहा हूँ।
2022-04-20 15:49:51.752 30358-31768/com.ashish.realtimecalltranscription I/RecorderService: 15.49.51 Final result received: चलो, मैं दूसरे एंड से बात कर रहा हूँ।
2022-04-20 15:49:55.429 30358-30358/com.ashish.realtimecalltranscription D/RecorderService: onReceive: Stopped transcription.
2022-04-20 15:49:56.218 30358-31768/com.ashish.realtimecalltranscription I/RecorderService: 15.49.56 Final result received: 
2022-04-20 15:49:56.225 30358-31771/com.ashish.realtimecalltranscription I/RecorderService: Continuous recognition stopped.
`````

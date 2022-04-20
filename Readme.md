## Sample Android Application for Azure Speech SDK troubleshooting

* Clone this repo and update `Constants.SpeechSubscriptionKey` with your subscription key.
* Build the APK and install on android 10/11.
* Allow permissions, allow it to be the device admin app and enable accessibility service.
* Get an incoming call and check logs under the tag `RecorderService`
* Note : Uninstalling the app requires the user to **manually uncheck the app from device admin app list**.

## Problem Faced
* We have been unable to transcribe **incoming call audio** on android 10/11. However, it works perfectly on older versions. We have used AudioRecord instance for the audio source as suggested by the sdkdemo sample github repository (https://github.com/Azure-Samples/cognitive-services-speech-sdk/tree/master/samples/java/android/sdkdemo). 

## Below are some logs for a recent call

* Between **13.52.09** and **13.52.45** audio processing wasn't achieved even after enabling the loudspeaker on intended device.
* **logs.txt** shows the entire stack trace when tested on android 11 - Unable to recognize audio.
* **logs-working-on-android-8.1.txt** shows the entire stack trace when tested on android 8.1 - Working ideally as expected.

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

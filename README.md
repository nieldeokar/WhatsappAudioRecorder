# WhatsappAudioRecorder
Clone of Whatsapp audio recording view.



### Using AudioRecorder class
While Integrating with my app I observed that sometimes Default MediaRecorder class does not releases the AudioFocus which stops
recording audion on next attempt and user does not get any clue why audio is not recording. Using AudioRecorder class we get raw 
PCM data and we can compare the expected recorded bytes and recorded audio bytes comparing which we can confirm the issue with 
gaining mic control and update user accordingly. I have stopped the timer if there is any problem with recording audio so user 
would understand.



#### Libraries used
* AppCompat
* [RecordView][1]



<a href="url"><img src="https://github.com/nieldeokar/WhatsappAudioRecorder/blob/master/images/recorder_1.png" align="left" height="600" width="350" ></a>

<a href="url"><img src="https://github.com/nieldeokar/WhatsappAudioRecorder/blob/master/images/recorder_2.png" align="right" height="600" width="350" ></a>

<a href="url"><img src="https://github.com/nieldeokar/WhatsappAudioRecorder/blob/master/images/recorder_3.png" align="left" height="600" width="350" ></a>

<a href="url"><img src="https://github.com/nieldeokar/WhatsappAudioRecorder/blob/master/images/recorder_4.png" align="right" height="600" width="350" ></a>


[1]: https://github.com/3llomi/RecordView

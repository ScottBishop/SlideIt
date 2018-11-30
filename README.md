# SlideIt
An android things motion sensor slackbot built with firebase realtime database, storage, cloud functions, and ML kit facial recognition.

Includes an Android mobile app connected to firebase to arm and disarm the bot as well as view recent photos.

### Workflow
1. Motion sensor detects motion
2. Image is taken
3. Firebase ML Kit determines if it was a person with facial recognition
4. If it was a person, it uploads the image to firebase realtime database
5. Firebase cloud functions hook for when an image is uploaded to the Firebase realtime database 
6. Cloud function sends slack message with the image to a specified channel

#### NXP i.MX7D development kit
https://developer.android.com/things/hardware/imx7d

#### PIR Motion Sensor
https://learn.sparkfun.com/tutorials/pir-motion-sensor-hookup-guide?_ga=2.237755200.1913013074.1541563285-314066326.1541563285

#### Based off of this tutorial:
https://www.hackster.io/riggaroo/smart-motion-sensing-camera-with-intruder-notifications-b6c613

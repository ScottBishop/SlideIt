# SlideIt
An Android Things motion sensor that takes a picture of someone going down the San Francisco slide and send the image to a specified slack channel.

It is built with firebase realtime database, storage, cloud functions, and ML Kit facial recognition.
Includes an Android mobile app connected to firebase to arm and disarm the bot as well as view recently uploaded photos.

### Workflow
1. Motion sensor detects a person going down the slide in our SF office.
2. Image is taken
3. Firebase ML Kit determines if it was a person with facial recognition
4. If it was a person, it uploads the image to firebase realtime database
5. Firebase cloud functions hook for when an image is uploaded to the Firebase realtime database 
6. Cloud function sends slack message with the image to a specified channel

### Deploy cloud functions
From the `cloud-functions/functions` directory, you can run
```
firebase deploy --project [project-id] --only functions
```

![img_1580](https://user-images.githubusercontent.com/1944329/49309049-d15f0980-f48e-11e8-9914-6571ee1ee11e.jpg)

#### NXP i.MX7D development kit
https://developer.android.com/things/hardware/imx7d

#### PIR Motion Sensor
https://learn.sparkfun.com/tutorials/pir-motion-sensor-hookup-guide?_ga=2.237755200.1913013074.1541563285-314066326.1541563285

#### Based off of this tutorial:
https://www.hackster.io/riggaroo/smart-motion-sensing-camera-with-intruder-notifications-b6c613

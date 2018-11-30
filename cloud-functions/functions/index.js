'use strict';

var functions = require('firebase-functions');
var admin = require('firebase-admin');
var request = require('request')
const vision = require('@google-cloud/vision');
const client = new vision.ImageAnnotatorClient();
admin.initializeApp(functions.config().firebase);
exports.sendImageToSlack = functions.database.ref('/motion-logs/{id}')
  .onCreate(event => {
    const original = event.data.val();
    const downloadUrl = original.downloadUrl;

    const filePath = 'gs://slideit-5c095.appspot.com' + original.imageRef;
    const faceRequest = {
        image: {
            source: { imageUri: filePath }
        }
    };

    console.log("Requesting face detection...", faceRequest);

    return client
        .faceDetection(faceRequest)
        .then(response => {
            const faces = response[0].faceAnnotations;
            if (!Array.isArray(faces) || !faces.length) {
                console.log("No face detected! Skipping slack message");
            } else {
                console.log("Face detected!", response);
                var payload = {
                    json: {
                        channel: functions.config().slack.channel,
                        icon_emoji: ":camera_with_flash:",
                        username: "SlideIt",
                        attachments: [
                            {
                                title: "We have a new slider!",
                                image_url: downloadUrl
                            }
                        ]
                    }
                    };

                    console.log('Sending slack message', event.params.id, original);
                    console.log('Slack message payload', payload);

                    return request.post(
                        functions.config().slack.webhook,
                        payload
                    );
                }
        })
        .catch(err => {
            console.log("No face detected!", err);
            console.error(err);
        });
  });

exports.annotateImage = functions.database.ref('/motion-logs/{id}')
  .onCreate(event => {
    const original = event.data.val();
    const downloadUrl = original.downloadUrl;
    console.log('AnnotatingImage', event.params.id, original);

    var topic = "/topics/sliders";

    var payload = {
      data: {
        title: "Slider Alert!",
        body: "A slider has been detected",
        image: downloadUrl,
        timestamp: original.timestamp.toString()
      }
    };
    return admin.messaging().sendToTopic(topic, payload)
      .then(function (response) {
        // See the MessagingTopicResponse reference documentation for the
        // contents of response.
        console.log("Successfully sent message:", response);
      })
      .catch(function (error) {
        console.log("Error sending message:", error);
      });
  }
  );

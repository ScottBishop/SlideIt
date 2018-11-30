'use strict';

var functions = require('firebase-functions');
var admin = require('firebase-admin');
var requestLib = require('request')
admin.initializeApp(functions.config().firebase);

exports.sendImageToSlack = functions.database.ref('/motion-logs/{id}')
  .onCreate(event => {
    const original = event.data.val();
    const downloadUrl = original.downloadUrl;

    var payload = {
      json: {
        channel: "@scott.bishop",
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

    return requestLib.post(
        functions.config().slack.webhook,
        payload
    );
  });

exports.annotateImage = functions.database.ref('/motion-logs/{id}')
  .onCreate(event => {
    const original = event.data.val();
    console.log('AnnotatingImage', event.params.id, original);
    const fileName = 'gs://slideit-5c095.appspot.com' + original.imageRef;
    console.log('Filename:', fileName)
    const request = {
      source: {
        imageUri: fileName
      }
    };

    var topic = "/topics/intruders";

    var payload = {
      data: {
        title: "Intruder Alert!",
        body: "An intruder has been detected",
        imageRef: original.imageRef,
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

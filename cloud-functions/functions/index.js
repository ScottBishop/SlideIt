'use strict';

var functions = require('firebase-functions');
var admin = require('firebase-admin');
var request = require('request')
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

    return request.post(
        functions.config().slack.webhook,
        payload
    );
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

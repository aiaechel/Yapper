const functions = require('firebase-functions');
const admin = require('firebase-admin');
const GeoFire = require('geofire');

admin.initializeApp(functions.config().firebase);
const geoFire = new GeoFire(admin.database().ref('/geofire'));


exports.saveChatroomToGeofire = functions.database.ref('/chatrooms/{pushId}/location')
  .onWrite(event => {
    const data_location = event.data.val();
    const location = [data_location.lat, data_location.lng];
    return geoFire.set(event.params.pushId, location);
  });


// Take the json location and radius within body and retrive all nearby chatrooms
// GET request using query string
// Example: /getNearbyChatrooms?lat=34.120000&lng=-118.030000&rad=5&user_id=3Cd3Ofde78hpg8mMsqTv5i2UB4M2
exports.getNearbyChatrooms = functions.https.onRequest((req, res) => {
  // Parse Query String
  const latitude = parseFloat(req.query.lat);
  const longitude = parseFloat(req.query.lng);
  const radius = parseFloat(req.query.rad);
  const user_id = req.query.user_id;

  var geoQuery = geoFire.query({
    center: [latitude, longitude],
    radius: radius
  });

  var foundKeys = [];
  var foundChatrooms = [];

  var onKeyEnteredRegistration = geoQuery.on("key_entered", function(key, location) {
    foundKeys.push(key);
  });

  var onReadyRegistration = geoQuery.on("ready", function() {
    // cancel query now that all keys have been found
    geoQuery.cancel();

    // map promises to retrieve all the chatroom data using the keys found in the area
    var promises = foundKeys.map(function(key, index) {
      return admin.database().ref(`/chatrooms/${key}`).once('value').then(snapshot => {
        var room_id = snapshot.key;
        var data = snapshot.val();

        return admin.database().ref(`/chatrooms/${key}/subscribers/${user_id}`).once('value').then(subscriberSnapshot => {
          var is_subscribed = false;
          if(subscriberSnapshot.exists()) {
            is_subscribed = true;
          }

          var data_json = {id: room_id, room_name: data.room_name, timestamp: data.timestamp, location: data.location, is_subscribed: is_subscribed};
          foundChatrooms.push(data_json);
        });
      });
    });

    // return json of data after resolving all promises
    Promise.all(promises).then(function() {
      res.json(foundChatrooms);
    });
  })
});


// Retrieve subscribed chatrooms for a user ID
// GET request using query string
// Example: /getSubscribedChatrooms?user_id=9Vo2jlDxgMR3CgeLoDN1h4T9H492
exports.getSubscribedChatrooms = functions.https.onRequest((req, res) => {
  // Parse Query String
  const user_id = req.query.user_id;

  admin.database().ref(`/users/${user_id}/subscribed`).once('value').then(snapshot => {
    // loop over each subscribed and push chatroom key into array
    var subscribed_chatrooms_ids = [];
    snapshot.forEach(childSnapshot => {
      subscribed_chatrooms_ids.push(childSnapshot.key);
    });

    // once all chatroom keys retrieved, fetch data using promises
    var subscribed_chatrooms_data = [];
    var promises = subscribed_chatrooms_ids.map(function(key, index) {
      // get chatroom data from ID
      return admin.database().ref(`/chatrooms/${key}`).once('value').then(roomSnapshot => {
        var room_id = roomSnapshot.key;
        var data = roomSnapshot.val();

        var data_json = {id: room_id, room_name: data.room_name, timestamp: data.timestamp, location: data.location};
        subscribed_chatrooms_data.push(data_json);
      });
    });

    // return json of data after resolving all promises
    Promise.all(promises).then(function() {
      res.json(subscribed_chatrooms_data);
    });
  });
});


// Send notifications to users subscribed to chatroom
exports.sendNotification = functions.database.ref('/chatrooms/{roomId}/messages/{messageId}')
  .onWrite(event => {
    // Exit if the data is deleted.
    if (!event.data.exists()) {
      return;
    }

    const message = event.data.current.val();
    const senderName = message.user_name;
    const messageBody = message.body;
    const senderId = message.user_id;

    const roomId = event.params.roomId;

    // get all users subscribed to chatroom
    return admin.database().ref(`/chatrooms/${roomId}/subscribers`).once('value').then(snapshot => {
      snapshot.forEach(childSnapshot => {
        const subscriberId = childSnapshot.key;
        const subscriberName = childSnapshot.val();

        if(subscriberId !== senderId) {
          // get instance IDs for subscriber
          admin.database().ref(`/users/${subscriberId}/instance_ids`).once('value').then(instanceIdsSnapshot => {
            instanceIdsSnapshot.forEach(instanceIdSnapshot => {
              const subscriberInstanceId = instanceIdSnapshot.val();

              // send notification to subscriber's instance ID
              console.log('notifying ' + subscriberInstanceId + ' about ' + messageBody + ' from ' + senderName);

              const payload = {
                data: {
                  room_id: roomId
                },
                notification: {
                  title: senderName,
                  body: messageBody,
                  click_action: "ACTIVITY_CHATROOM"
                }
              };

              admin.messaging().sendToDevice(subscriberInstanceId, payload)
                .then(function (response) {
                  console.log("Successfully sent message:", response);
                })
                .catch(function (error) {
                  console.log("Error sending message:", error);
                });
            });
          });
        }
      });
    });
  });


// Add chatroom as a POST request
// Mainly used for testing purposes
exports.addChatroom = functions.https.onRequest((req, res) => {
  // Grab the text parameter.
  const name = req.body.name;
  const location = req.body.location;

  // Generate Timestamp
  const timestamp = admin.database.ServerValue.TIMESTAMP;

  // Push it into the Realtime Database then send a response
  const data = {timestamp: timestamp, name: name, location: location};
  admin.database().ref('/chatrooms/').push(data).then(snapshot => {
    res.send(200, "ok");
  });
});

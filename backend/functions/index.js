const functions = require('firebase-functions');
const admin = require('firebase-admin');
const GeoFire = require('geofire');

admin.initializeApp(functions.config().firebase);
const geoFire = new GeoFire(admin.database().ref('/geofire'));

// Take the query and json parameter passed to message and insert it into the
// Realtime Database under the path /chatrooms/:roomId/messages/:pushId/
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

exports.saveChatroomToGeofire = functions.database.ref('/chatrooms/{pushId}/location')
    .onWrite(event => {
      const data_location = event.data.val();
      const location = [data_location.lat, data_location.lng];
      return geoFire.set(event.params.pushId, location);
    });

// Take the query and json parameter passed to message and insert it into the
// Realtime Database under the path /chatrooms/:roomId/messages/:pushId/
exports.getNearbyChatrooms = functions.https.onRequest((req, res) => {
  // Grab the text parameter.
  const location = req.body.location;

  var geoQuery = geoFire.query({
    center: [location.lat, location.lng],
    radius: req.body.rad
  });

  var foundKeys = [];
  var foundChatrooms = [];

  var onKeyEnteredRegistration = geoQuery.on("key_entered", function(key, location) {
    foundKeys.push(key);
  });

  var onReadyRegistration = geoQuery.on("ready", function() {
    console.log("*** Found " + foundKeys.length + " chatrooms! ***");
    geoQuery.cancel();

    var promises = foundKeys.map(function(key, index) {
      return admin.database().ref('/chatrooms/' + key).once('value').then(snapshot => {
        var room_id = snapshot.key;
        var data = snapshot.val();

        var data_json = {id: room_id, name: data.name, timestamp: data.timestamp, location: data.location};
        foundChatrooms.push(data_json);
      });
    });

    Promise.all(promises).then(function() {
      res.json(foundChatrooms);
    });
  })
});

// // Listens for new messages added to /messages/:pushId/original and creates an
// // uppercase version of the message to /messages/:pushId/uppercase
// exports.makeUppercase = functions.database.ref('/messages/{pushId}/original')
//     .onWrite(event => {
//       // Grab the current value of what was written to the Realtime Database.
//       const original = event.data.val();
//       console.log('Uppercasing', event.params.pushId, original);
//       const uppercase = original.toUpperCase();
//       // You must return a Promise when performing asynchronous tasks inside a Functions such as
//       // writing to the Firebase Realtime Database.
//       // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
//       return event.data.ref.parent.child('uppercase').set(uppercase);
//     });
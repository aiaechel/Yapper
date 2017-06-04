const functions = require('firebase-functions');
const admin = require('firebase-admin');
const GeoFire = require('geofire');

admin.initializeApp(functions.config().firebase);
const geoFire = new GeoFire(admin.database().ref('/geofire'));

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

exports.saveChatroomToGeofire = functions.database.ref('/chatrooms/{pushId}/location')
    .onWrite(event => {
      const data_location = event.data.val();
      const location = [data_location.lat, data_location.lng];
      return geoFire.set(event.params.pushId, location);
    });

// Take the json location and radius within body and retrive all nearby chatrooms
// GET request using query string
// Example: /getNearbyChatrooms?lat=-57.030000&lng=34.120000&rad=5
exports.getNearbyChatrooms = functions.https.onRequest((req, res) => {
  // Parse Query String
  const latitude = parseFloat(req.query.lat);
  const longitude = parseFloat(req.query.lng);
  const radius = parseFloat(req.query.rad);

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
      return admin.database().ref('/chatrooms/' + key).once('value').then(snapshot => {
        var room_id = snapshot.key;
        var data = snapshot.val();

        var data_json = {id: room_id, name: data.name, timestamp: data.timestamp, location: data.location};
        foundChatrooms.push(data_json);
      });
    });

    // return json of data after resolve all promises
    Promise.all(promises).then(function() {
      res.json(foundChatrooms);
    });
  })
});
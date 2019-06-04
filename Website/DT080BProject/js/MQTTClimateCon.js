//Using Hive broker - dashboard available at http://www.hivemq.com/demos/websocket-client/
//Uses the Paho MQTT JS client library - http://www.eclipse.org/paho/files/jsdoc/index.html to send and receive messages using a web browser
//Example code available at https://www.hivemq.com/blog/mqtt-client-library-encyclopedia-paho-js

var topic = "JavaTest";
var url = "broker.mqttdashboard.com";
var port = "8000";

window.addEventListener('load', connectToBroker);
var roomOneEnButt = document.getElementById("roomOneOnButton");
var roomOneDisButt = document.getElementById("roomOneOffButton");

var maxSliderIn = document.getElementById("maxSlider");
var maxSliderOut = document.getElementById("maxRangeSpan");
var minSliderIn = document.getElementById("minSlider");
var minSliderOut = document.getElementById("minRangeSpan");

maxSliderIn.oninput = function()
{
    maxSliderOut.innerHTML = this.value+"&deg;C";
}
minSliderIn.oninput = function()
{
    minSliderOut.innerHTML = this.value+"&deg;C";
}
// Create a client instance
client = new Paho.MQTT.Client(url, 8000, "web_" + parseInt(Math.random() * 100, 10));
client.qos = 0;
client.onMessageArrived = onMessageArrived;

// set callback handlers
//client.onConnected = onConnected;
client.onConnectionLost = onConnectionLost;
 
var connectOptions = {
    onSuccess: onConnectCallback //other options available to set
};

function connectToBroker(){
    // connect the client
    client.connect(connectOptions);
}
function disconnectFromBroker(){
  // disconnect the client
  client.disconnect();
  console.log("Disconnected");
}
var subOptions = {
  //assign the callback method when we successfully subscribe to a topic
  onSuccess: subscribeCallback
}

function subscribeToTopic(){
  //subscribe to that topic
  client.subscribe(topic,subOptions);
}
//the function that is called when an MQTT message is received
function onMessageArrived(data) {

}
//this function is called when there is a successful subscription
function subscribeCallback()
{
  //set the subscription flag to 1
  subFlag = 1;
  //disable the publish button, in my program I would like
  //to only allow a publish when we are subscribed already
  console.log("subscribed");
}
// called when the client connect request is successful
function onConnectCallback() {
  subscribeToTopic();
  // Once a connection has been made, set the connect flag
  connectFlag = 1;
}

// called when the client loses its connection
function onConnectionLost(responseObject) {
  if (responseObject.errorCode !== 0) {
    console.log("onConnectionLost:"+responseObject.errorMessage);
  }
}

//this function is called when the publish button is clicked
function publishToTopic(message){
   client.publish(topic, message, 0, false);
}

function setMaxTemp()
{
  var maxTemp = document.getElementById("maxSlider").value; 
  if(maxTemp < document.getElementById("minSlider").value{
    maxTemp = document.getElementById("minSlider").value;
  }
  publishToTopic("MAX: "+maxTemp);
}
function setMinTemp()
{
  var minTemp = document.getElementById("minSlider").value;
  if(minTemp < document.getElementById("maxSlider").value{
    maxTemp = document.getElementById("maxSlider").value;
  }
  publishToTopic("MIN: "+minTemp);
}

//Using Hive broker - dashboard available at http://www.hivemq.com/demos/websocket-client/
//Uses the Paho MQTT JS client library - http://www.eclipse.org/paho/files/jsdoc/index.html to send and receive messages using a web browser
//Example code available at https://www.hivemq.com/blog/mqtt-client-library-encyclopedia-paho-js

var topic = "JavaTest";
var url = "broker.mqttdashboard.com";
var port = "8000";

window.addEventListener('load', connectToBroker);
var roomOneEnButt = document.getElementById("roomOneOnButton");
var roomOneDisButt = document.getElementById("roomOneOffButton");
var roomTwoEnButt = document.getElementById("roomTwoOnButton");
var roomTwoDisButt = document.getElementById("roomTwoOffButton");
var roomThreeEnButt = document.getElementById("roomThreeOnButton");
var roomThreeDisButt = document.getElementById("roomThreeOffButton");


//disable all buttons until rooms are alive
roomOneEnButt.disabled = true;
roomOneEnButt.style.background = "grey";
roomOneDisButt.disabled = true;
roomOneDisButt.style.background = "grey";
roomTwoEnButt.disabled = true;
roomTwoEnButt.style.background = "grey";
roomTwoDisButt.disabled = true;
roomTwoDisButt.style.background = "grey";
roomThreeEnButt.disabled = true;
roomThreeEnButt.style.background = "grey";
roomThreeDisButt.disabled = true;
roomThreeDisButt.style.background = "grey";


//Flags are used to check the state of interactions

//These arrays and variables are used for the chart

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
  //When we receive a new message redraw the chart
  //this allows it to update live
  console.log("Message Arrived:"+data.payloadString);
  //store the incoming message
  var str = data.payloadString;
  console.log(str);
  //clear any white spaces from the message 
  str = str.trim();
  var mesCode = str.substring(0, 4);
  console.log(mesCode);
  str.trim();
  //if the message is on or off then it is not for us
  //it is for turning on the light, so ignore it
  if(mesCode.valueOf() == "HTR:")
  {
    var roomNum = str.substring(5, 11);
    console.log(roomNum);
    roomNum.trim();
    if(roomNum.valueOf() == "Room 1")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      if(res.valueOf("0")){
         document.getElementById("roomOneHeatStatusP").innerHTML = "Heater Status: Off";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomOneHeatStatusP").innerHTML = "Heater Status: On";
      }
    }
    else if(roomNum.valueOf() == "Room 2")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      console.log(res);
      if(res.valueOf("0")){
        document.getElementById("roomTwoHeatStatusP").innerHTML = "Heater Status: Off";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomTwoHeatStatusP").innerHTML = "Heater Status: on";
      }
    }
    else if(roomNum.valueOf() == "Room 3")
    {
      var res = str.substring(12);
      console.log(res);
      if(res.valueOf("0")){
        document.getElementById("roomThreeHeatStatusP").innerHTML = "Heater Status: Off";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomThreeHeatStatusP").innerHTML = "Heater Status: on";
      }
    }
  }
  else if(mesCode.valueOf() == "FAN:")
  {
    var roomNum = str.substring(5, 11);
    console.log(roomNum);
    roomNum.trim();
    if(roomNum.valueOf() == "Room 1")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      if(res.valueOf("0")){
         document.getElementById("roomOneFanStatusP").innerHTML = "Fan Status: Off";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomOneFanStatusP").innerHTML = "Fan Status: On";
      }
    }
    else if(roomNum.valueOf() == "Room 2")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      console.log(res);
      if(res.valueOf("0")){
        document.getElementById("roomTwoFanStatusP").innerHTML = "Fan Status: Off";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomTwoFanStatusP").innerHTML = "Fan Status: on";
      }
    }
    else if(roomNum.valueOf() == "Room 3")
    {
      var res = str.substring(12);
      console.log(res);
      if(res.valueOf("0")){
        document.getElementById("roomThreeFanStatusP").innerHTML = "Fan Status: Off";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomThreeFanStatusP").innerHTML = "Fan Status: on";
      }
    }
  }
  else if(mesCode.valueOf() == "ALV:")
  {
    var roomNum = str.substring(5, 11);
    console.log(roomNum);
    roomNum.trim();
    if(roomNum.valueOf() == "Room 1")
    {
      document.getElementById("roomOneOnOffStatusP").innerHTML = "Room 1: Alive";
      roomOneEnButt.disabled = false;
      roomOneEnButt.style.background = "linear-gradient(to right,royalblue, #663399)";
      roomOneDisButt.disabled = false;
      roomOneDisButt.style.background = "linear-gradient(to right,royalblue, #663399)";
    }
    else if(roomNum.valueOf() == "Room 2")
    {
      document.getElementById("roomTwoOnOffStatusP").innerHTML = "Room 2: Alive";
      roomTwoEnButt.disabled = false;
      roomTwoEnButt.style.background = "linear-gradient(to right,royalblue, #663399)";
      roomTwoDisButt.disabled = false;
      roomTwoDisButt.style.background = "linear-gradient(to right,royalblue, #663399)";
    }
    else if(roomNum.valueOf() == "Room 3")
    {
      document.getElementById("roomThreeOnOffStatusP").innerHTML = "Room 3: Alive";
      roomThreeEnButt.disabled = false;
      roomThreeEnButt.style.background = "linear-gradient(to right,royalblue, #663399)";
      roomThreeDisButt.disabled = false;
      roomThreeDisButt.style.background = "linear-gradient(to right,royalblue, #663399)";
    }
  }
  else if(mesCode.valueOf() == "BLD:")
  {
    var roomNum = str.substring(5, 11);
    console.log(roomNum);
    roomNum.trim();
    if(roomNum.valueOf() == "Room 1")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      if(res.valueOf("0")){
         document.getElementById("roomOneBlindStatusP").innerHTML = "Blind Status: Closed";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomOneBlindStatusP").innerHTML= "Blind Status: Open";
      }
    }
    else if(roomNum.valueOf() == "Room 2")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      console.log(res);
      if(res.valueOf("0")){
        document.getElementById("roomTwoBlindStatusP").innerHTML = "Blind Status: Closed";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomTwoBlindStatusP").innerHTML = "Blind Status: Open";
      }
    }
    else if(roomNum.valueOf() == "Room 3")
    {
      var res = str.substring(12);
      console.log(res);
      if(res.valueOf("0")){
        document.getElementById("roomThreeBlindStatusP").innerHTML = "Blind Status: Closed";
      }
      else if(res.valueOf("1")){
        document.getElementById("roomThreeBlindStatusP").innerHTML = "Blind Status: Open";
      }
    }
  }
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
  if (responseObject.errorCode != 0) {
    console.log("onConnectionLost:"+responseObject.errorMessage);
  }
}

//this function is called when the publish button is clicked
function publishToTopic(message){
   client.publish(topic, message, 0, false);
}

function activateRoom(roomNum)
{
  if(roomNum == 1)
  {
     publishToTopic("ENB: Room 1 ALL 1");
  }
  else if(roomNum == 2)
  {
     publishToTopic("ENB: Room 2 ALL 1");
  }
  else if(roomNum == 3)
  {
    publishToTopic("ENB: Room 3 ALL 1");
  }
  actLightResponse();
   
}
function deactivateRoom(roomNum)
{
  if(roomNum == 1)
  {
     publishToTopic("ENB: Room 1 ALL 0");
  }
  else if(roomNum == 2)
  {
     publishToTopic("ENB: Room 2 ALL 0");
  }
  else if(roomNum == 3)
  {
    publishToTopic("ENB: Room 3 ALL 0");
  }
  deActLightResponse();
}

function actLightResponse()
{
    document.getElementById('backImgActShadow').style.animationName = "unfadeShadow"
    document.getElementById('backImgActShadow').style.animationDuration = "8s"
    document.getElementById('backImgActShadow').style.animationTimingFunction = "ease-in-out"
    document.getElementById('backImgActShadow').style.animation = "unfadeShadow 8s forwards"
    stopRaining = setTimeout(reverseShadow, 8000); 
}
function reverseShadow()
{
    document.getElementById('backImgActShadow').style.animationName = ""
    document.getElementById('backImgActShadow').style.animationDuration = ""
    document.getElementById('backImgActShadow').style.animationTimingFunction = ""
}
function deActLightResponse()
{
    document.getElementById('backImgActDarken').style.animationName = "unfadeDarken"
    document.getElementById('backImgActDarken').style.animationDuration = "8s"
    document.getElementById('backImgActDarken').style.animationTimingFunction = "ease-in-out"
    document.getElementById('backImgActDarken').style.animation = "unfadeDarken 8s forwards"
    stopRaining = setTimeout(reverseDarken, 8000); 
}
function reverseDarken()
{
    document.getElementById('backImgActDarken').style.animationName = ""
    document.getElementById('backImgActDarken').style.animationDuration = ""
    document.getElementById('backImgActDarken').style.animationTimingFunction = ""
}

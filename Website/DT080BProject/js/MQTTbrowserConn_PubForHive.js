//Using Hive broker - dashboard available at http://www.hivemq.com/demos/websocket-client/
//Uses the Paho MQTT JS client library - http://www.eclipse.org/paho/files/jsdoc/index.html to send and receive messages using a web browser
//Example code available at https://www.hivemq.com/blog/mqtt-client-library-encyclopedia-paho-js

var topic = "JavaTest";
var url = "broker.mqttdashboard.com";
var port = "8000";

window.addEventListener('load', connectToBroker);

//Flags are used to check the state of interactions
var connectFlag = 0;
var subFlag = 0;
var chartOneFlag = 0;
var chartTwoFlag = 0;
var chartThreeFlag = 0;

//These arrays and variables are used for the chart
var roomOneArray = [];
var roomTwoArray = [];
var roomThreeArray = [];
var hourOneArray = [];
var minOneArray = [];
var secOneArray = [];
var hourTwoArray = [];
var minTwoArray = [];
var secTwoArray = [];
var hourThreeArray = [];
var minThreeArray = [];
var secThreeArray = [];
var roomOneArrayIndex = 0;
var roomTwoArrayIndex = 0;
var roomThreeArrayIndex = 0;
var timeArrayIndex = 0;
var hourArray = [];
var minArray = [];
var secArray = [];

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
  if(mesCode.valueOf() == "TMP:")
  {
    console.log("testy");
    var roomNum = str.substring(5, 11);
    console.log(roomNum);
    roomNum.trim();
    if(roomNum.valueOf() == "Room 1")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      console.log(res);
      var total = parseInt(res);
      //get the current time and date for the chart
      var today = new Date();
      //strip the hours, minutes and seconds and store them 
      //in an array for use in the chart
     /* hourOneArray[roomOneArrayIndex] = today.getHours();
      minOneArray[roomOneArrayIndex] = today.getMinutes();
      secOneArray[roomOneArrayIndex] = today.getSeconds(); */

      hourOneArray[roomOneArrayIndex] = today.getHours();
      minOneArray[roomOneArrayIndex] = today.getMinutes();
      secOneArray[roomOneArrayIndex] = today.getSeconds();
      //store the total into an array for the chart value
      roomOneArray[roomOneArrayIndex] = total;
      //increment the array index so we know how many times to draw 
      //a plot point on the chart
      roomOneArrayIndex++;
      addChartOneScript()
    }
    else if(roomNum.valueOf() == "Room 2")
    {
      //split the message into two separate bytes
      var res = str.substring(12);
      var total = parseInt(res);
      console.log(total);
      //get the current time and date for the chart
      var today = new Date();
      //strip the hours, minutes and seconds and store them 
      //in an array for use in the chart
      /*hourTwoArray[roomTwoArrayIndex] = today.getHours();
      minTwoArray[roomTwoArrayIndex] = today.getMinutes();
      secTwoArray[roomTwoArrayIndex] = today.getSeconds();*/

      hourTwoArray[roomTwoArrayIndex] = today.getHours();
      minTwoArray[roomTwoArrayIndex] = today.getMinutes();
      secTwoArray[roomTwoArrayIndex] = today.getSeconds();
      //store the total into an array for the chart value
      roomTwoArray[roomTwoArrayIndex] = total;
      //increment the array index so we know how many times to draw 
      //a plot point on the chart
      roomTwoArrayIndex++;
      addChartTwoScript()
    }
    else if(roomNum.valueOf() == "Room 3")
    {
      var res = str.substring(12);
      var total = parseInt(res);
      //get the current time and date for the chart
      var today = new Date();
      //strip the hours, minutes and seconds and store them 
      //in an array for use in the chart
     /* hourThreeArray[roomThreeArrayIndex] = today.getHours();
      minThreeArray[roomThreeArrayIndex] = today.getMinutes();
      secThreeArray[roomThreeArrayIndex] = today.getSeconds();*/

      hourThreeArray[roomThreeArrayIndex] = today.getHours();
      minThreeArray[roomThreeArrayIndex] = today.getMinutes();
      secThreeArray[roomThreeArrayIndex] = today.getSeconds();
      //store the total into an array for the chart value
      roomThreeArray[roomThreeArrayIndex] = total;
      //increment the array index so we know how many times to draw 
      //a plot point on the chart
      roomThreeArrayIndex++;
      addChartThreeScript()
    }
    else
    {

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
  if (responseObject.errorCode !== 0) {
    console.log("onConnectionLost:"+responseObject.errorMessage);
  }
}

//this function is called when the publish button is clicked
function publishToTopic(){
   var message = document.getElementById("publishMessage").value;
   var topic = document.getElementById("topicInput").value;
   client.publish(topic, message, 0, false);
}


//called when the show chart button is clicked
function checkChartOne()
{
  //chart flag is set here so the chart will only been drawn when the button
  //is clicked instead of when we read a value first
  chartOneFlag = 1;
  addChartOneScript();
}
//called when the show chart button is clicked
function checkChartTwo()
{
  //chart flag is set here so the chart will only been drawn when the button
  //is clicked instead of when we read a value first
  chartTwoFlag = 1;
  addChartTwoScript();
}
//called when the show chart button is clicked
function checkChartThree()
{
  //chart flag is set here so the chart will only been drawn when the button
  //is clicked instead of when we read a value first
  chartThreeFlag = 1;
  addChartThreeScript();
}
function addChartOneScript()
{
  if(chartOneFlag == 1)
  {
    google.charts.load('current', {'packages':['corechart']}); //load the line chart options from google charts
    google.charts.setOnLoadCallback(drawOneChart);
    function drawOneChart() //This function will draw the chart with the data and options supplied
    {
        var data = new google.visualization.DataTable();
        //the columns are the x and y axis values
        data.addColumn('timeofday', 'Time');
        data.addColumn('number', 'Room 2');
        //draw each row with the values for time in the x axis
        //and the values for the soil humidity in the y axis
          for (i = 0; i < roomOneArrayIndex; i++) 
          {
              data.addRow([[hourOneArray[i],minOneArray[i],secOneArray[i]], roomOneArray[i]]);
          }
        //The options set how the chart appears on the page
        var options = 
        {
            title: 'Room 1',
            curveType: 'function',

            lineWidth: '2',
            theme: 'maximized',
            backgroundColor: 'transparent',
            colors:['hotpink'],
            pointSize:'2',
            //chartArea:{left:'10%',top:'10%',width:'100%',height:'80%'},
            titleTextStyle:
            {
                color:'hotpink'
            },
            vAxis: 
            {
                minValue:0, maxValue:40,
                format: '#\'°C\'',
                textStyle:
                {
                    color:'#ffffff'
                },
                titleTextStyle:
                {
                    color:'#ffffff'
                },
                gridlines:
                {
                    color:'grey'
                }
            },
            baselineColor: 'grey',
            hAxis:
            {
                gridlines:
                {
                    color:'grey'
                },
                titleTextStyle:
                {
                    color:'#ffffff'
                },
                textStyle:
                {
                    color:'hotpink'
                },
            },
            legend:
            {
                position: 'bottom',
                textStyle:
                {
                    color:'#54ced4'
                }
            },
            animation: { duration: 1000,
            easing: 'in'}
        };

        var chart = new google.visualization.LineChart(document.getElementById('roomOne_curve_chart')); //set the chart document in javascript

        chart.draw(data, options); //Draw the chart
        window.addEventListener("resize", resizeChart); //When the window is resized, then redraw the chart
        function resizeChart()
        {
            chart.draw(data, options);
        }
  
    }
  }
}
function addChartTwoScript()
{
  if(chartTwoFlag == 1)
  {
    google.charts.load('current', {'packages':['corechart']}); //load the line chart options from google charts
    google.charts.setOnLoadCallback(drawTwoChart);
    function drawTwoChart() //This function will draw the chart with the data and options supplied
    {
        var data = new google.visualization.DataTable();
        //the columns are the x and y axis values
        data.addColumn('timeofday', 'Time');
        data.addColumn('number', 'Room 2');
        //draw each row with the values for time in the x axis
        //and the values for the soil humidity in the y axis
          for (i = 0; i < roomTwoArrayIndex; i++) 
          {
              data.addRow([[hourTwoArray[i],minTwoArray[i],secTwoArray[i]], roomTwoArray[i]]);
          }
        //The options set how the chart appears on the page
        var options = 
        {
            title: 'Room 2',
            curveType: 'function',

            lineWidth: '2',
            theme: 'maximized',
            backgroundColor: 'transparent',
            colors:['cyan'],
            pointSize:'2',
            //chartArea:{left:'10%',top:'10%',width:'100%',height:'80%'},
            titleTextStyle:
            {
                color:'cyan'
            },
            vAxis: 
            {
                minValue:0, maxValue:40,
                format: '#\'°C\'',
                textStyle:
                {
                    color:'#ffffff'
                },
                titleTextStyle:
                {
                    color:'#ffffff'
                },
                gridlines:
                {
                    color:'grey'
                }
            },
            baselineColor: 'grey',
            hAxis:
            {
                gridlines:
                {
                    color:'grey'
                },
                titleTextStyle:
                {
                    color:'#ffffff'
                },
                textStyle:
                {
                    color:'hotpink'
                },
            },
            legend:
            {
                position: 'bottom',
                textStyle:
                {
                    color:'#54ced4'
                }
            },
            animation: { duration: 1000,
            easing: 'in'}
        };

        var chart = new google.visualization.LineChart(document.getElementById('roomTwo_curve_chart')); //set the chart document in javascript

        chart.draw(data, options); //Draw the chart
        window.addEventListener("resize", resizeChart); //When the window is resized, then redraw the chart
        function resizeChart()
        {
            chart.draw(data, options);
        }
  
    }
  }
}
function addChartThreeScript()
{
  if(chartThreeFlag == 1)
  {
    google.charts.load('current', {'packages':['corechart']}); //load the line chart options from google charts
    google.charts.setOnLoadCallback(drawThreeChart);
    function drawThreeChart() //This function will draw the chart with the data and options supplied
    {
        var data = new google.visualization.DataTable();
        //the columns are the x and y axis values
        data.addColumn('timeofday', 'Time');
        data.addColumn('number', 'Room 3');

        //draw each row with the values for time in the x axis
        //and the values for the soil humidity in the y axis

        for (i = 0; i < roomThreeArrayIndex; i++) 
        {
            data.addRow([[hourThreeArray[i],minThreeArray[i],secThreeArray[i]], roomThreeArray[i]]);
        }
 
        //The options set how the chart appears on the page
        var options = 
        {
            title: 'Room 3',
            curveType: 'function',

            lineWidth: '2',
            theme: 'maximized',
            backgroundColor: 'transparent',
            colors:['#13FF0F',],
            pointSize:'2',
            //chartArea:{left:'10%',top:'10%',width:'100%',height:'80%'},
            titleTextStyle:
            {
                color:'#13FF0F'
            },
            vAxis: 
            {
                minValue:0, maxValue:40,
                format: '#\'°C\'',
                textStyle:
                {
                    color:'#ffffff'
                },
                titleTextStyle:
                {
                    color:'#ffffff'
                },
                gridlines:
                {
                    color:'grey'
                }
            },
            baselineColor: 'grey',
            hAxis:
            {
                gridlines:
                {
                    color:'grey'
                },
                titleTextStyle:
                {
                    color:'#ffffff'
                },
                textStyle:
                {
                    color:'hotpink'
                },
            },
            legend:
            {
                position: 'bottom',
                textStyle:
                {
                    color:'#54ced4'
                }
            },
            animation: { duration: 1000,
            easing: 'in'}
        };

        var chart = new google.visualization.LineChart(document.getElementById('roomThree_curve_chart')); //set the chart document in javascript

        chart.draw(data, options); //Draw the chart
        window.addEventListener("resize", resizeChart); //When the window is resized, then redraw the chart
        function resizeChart()
        {
            chart.draw(data, options);
        }
  
    }
  }
}

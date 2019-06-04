<!DOCTYPE html>
<?php
    session_start();
    if(!isset($_SESSION["login"]))
    {
        $_SESSION['error']="You need to be logged in to access that resource"; 
        header('Location:login.php');
    }
?>
<html>
    <head>
        <!--favicon image for title bar-->
		<link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
		<link rel="icon" href="images/favicon.ico" type="images/x-icon">
        
        <meta charset="utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>Live Data</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" type="text/css" media="screen" href="css/style.css" /> 
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Allerta+Stencil">
        <meta name="format-detection" content="telephone=no">
        <meta name="msapplication-tap-highlight" content="no">
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width">		

    </head>

    <body id="indexBody" class="body">
        <div id="mySidenav" class="sidenav">
                <a href="javascript:void(0)" class="closebtn" onclick="closeChartNav()">&times;</a>
                <a href="index.php">Home</a>
                <a href="homepage.php">Select Data</a>
                <a href="activate.php">Activate Devices</a>
                <a href="climateControl.php">Climate Control</a>
                <a href="liveCharts.php">Current Data</a>
                <?php
                    if(!isset($_SESSION["login"]))
                    {
                        echo '<a href="login.php">Login</a>';
                        echo '<a href="register.php">Register</a>';
                    }
                    else
                    {
                        echo '<a href="logout.php">Logout</a>';

                    }
                ?>
                <a href="about.html">About</a>
        </div>
        <div id="main" style="margin-left:0%;">
            <span style="font-size:3vw;cursor:pointer; color:white;" onclick="openChartNav()">&#9776;</span>
        </div>
        <div id="indexBodyDiv" style="background-color:#48494f">
            
            <div id="fadeDiv">
            </div>
            <div id="contentBody" style="z-index: -4">

            </div>
            <div id="flashDiv">

            </div>
            <div class="splitDiv" id="topDiv"> <h1 id="aboutTitle"></h1> </div>
            <div class="splitDiv" id="midLeftDiv">
                <div id="cameraDiv">
                    <div id="roomOneChart" style="width: 100%; height: 100%">
                        <div id="roomOne_curve_chart" style="width: 100%; height: 100%"></div> 
                    </div>			
                </div>
            </div>
            <div class="splitDiv" id="midCentDiv">
                <div id="cameraDiv">
                    <div id="roomTwoChart" style="width: 100%; height: 100%">
                        <div id="roomTwo_curve_chart" style="width: 100%; height: 100%"></div> 
                    </div>			
                </div>
            </div>
            <div class="splitDiv" id="midRightDiv">            
                <div id="roomThreeChart" style="width: 100%; height: 100%">
                    <div id="roomThree_curve_chart" style="width: 100%; height: 100%"></div> 
                </div>
            </div>
            <div class="splitDiv" id="botDiv">
                <div id="botDiv1"> <button class="bottomButton" id="pumpOnButton" onclick="checkChartOne()">Room 1 Chart</button> </div>
                <div id="botDiv2"> </div>
                <div id="botDiv3"> <button class="bottomButton" id="pumpOffButton" onclick="checkChartTwo()">Room 2 Chart</button></div>
                <div id="botDiv4">  </div>
                <div id="botDiv5"> <button class="bottomButton" id="chartButton" onclick="checkChartThree()">Room 3 Chart</div>
                <div id="botDiv6"> </div>
            </div>
        </div>
        <!-- include link to js files required -->
        <script type="text/javascript" src="js/paho-mqtt.js" ></script>
        <!--This is a reference to the Paho Javascript client side browser based library-->
        <script type="text/javascript" src="js/MQTTbrowserConn&PubForHive.js"></script>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/1.20.2/TweenMax.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/1.20.2/plugins/CSSPlugin.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/1.20.2/plugins/TextPlugin.min.js"></script>     
        <script type="text/javascript" src="js/animTwo.js"></script> 

        <script type="text/javascript" src="js/js.js"></script> 

        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
        <script type="text/javascript"> </script>        
    </body>
</html>

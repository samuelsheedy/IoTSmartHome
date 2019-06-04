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
        <title>Activate Devices</title>
        <link rel="icon" type="image/png" href="images/logoIcon.ico">
        
        <!--favicon image for title bar-->
		<link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
        <link rel="icon" href="images/favicon.ico" type="images/x-icon">
        
         
        <!--make website responsive for viewing on all devices-->
        <!--  <link rel="stylesheet" type="text/css" href="css/responsive.css" /> -->
        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/1.20.2/TweenMax.min.js"></script>
        <link rel="stylesheet" type="text/css" href="css/stylesheet.css">
        <script type="text/javascript" src="js/js.js"></script>      
		    
    </head>
    <body id="activateBody" style="background-color:rgb(72, 73, 79)">
       <div>
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
            <div id="main" style="margin-left:10%;">
                <span style="font-size:3vw;cursor:pointer; color:white;" onclick="openChartNav()">&#9776; TU SMART HOME</span>
            </div>
            <div id="fadeDiv">
            </div>
            <div id="bannerAct">
                <img id="flowerImg" src="images/logo.png" alt="logo">
                <div id="backImgAct">
                </div>
                <div id="backImgActAnim">
                </div>
                <div id="backImgActShadow">
                </div>
                <div id="backImgActDarken">
                </div>
                <div id="lightAnim">
                </div>
            </div>
            <nav class="navBar">
                <ul class="navList" style="color:white">
                    <li class="leftItem"></li>
                    <li class="leftItem"></li>
                    <li class="leftItem"></li>
                    <li class="leftItem"></li>
                </ul> 
            </nav>
            <nav class="rightBar">
                <ul class="navList" style="color:white">
                    <li class="rightItem"></li>
                    <li class="rightItem"></li>
                    <li class="rightItem"></li>
                    <li class="rightItem"></li>
                </ul> 
            </nav>
            <div id="buttonContainer">
                <div class="buttonSplit">
                    <p class="roomStatus" id="roomOneOnOffStatusP" style="color:white;">Room 1 Status: Listening for message</p>
                    <button class="actButton" id="roomOneOnButton" type="button"  onclick="activateRoom(1)">Room One Enable</button>
                    <button class="actButton" id="roomOneOffButton" type="button"  onclick="deactivateRoom(1)">Room One Disable</button>
                    <p class="roomStatus" id="roomOneHeatStatusP" style="color:white;">Heater Status: </p>
                    <p class="roomStatus" id="roomOneFanStatusP" style="color:white;">Fan Status: </p>
                    <p class="roomStatus" id="roomOneBlindStatusP" style="color:white;">Blind Status: </p>                   
                </div>
                <div class="buttonSplit">
                    <p class="roomStatus" id="roomTwoOnOffStatusP" style="color:white;">Room 2 Status: Listening for message</p>
                    <button class="actButton" id="roomTwoOnButton" type="button"  onclick="activateRoom(2)">Room Two Enable</button>
                    <button class="actButton" id="roomTwoOffButton" type="button"  onclick="deactivateRoom(2)">Room Two Disable</button>
                    <p class="roomStatus" id="roomTwoHeatStatusP" style="color:white;">Heater Status: </p>
                    <p class="roomStatus" id="roomTwoFanStatusP" style="color:white;">Fan Status: </p>
                    <p class="roomStatus" id="roomTwoBlindStatusP" style="color:white;">Blind Status: </p>
                </div>
                <div class="buttonSplit">
                    <p class="roomStatus" id="roomThreeOnOffStatusP" style="color:white;">Room 3 Status: Listening for message</p>
                    <button class="actButton" id="roomThreeOnButton" type="button"  onclick="activateRoom(3)">Room Three Enable</button>
                    <button class="actButton" id="roomThreeOffButton" type="button"  onclick="deactivateRoom(3)">Room Three Disable</button>
                    <p class="roomStatus" id="roomThreeHeatStatusP" style="color:white;">Heater Status: </p>
                    <p class="roomStatus" id="roomThreeFanStatusP" style="color:white;">Fan Status: </p>
                    <p class="roomStatus" id="roomThreeBlindStatusP" style="color:white;">Blind Status: </p>
                </div>
            </div>

       </div>
        <!-- include link to js files required -->
        <script type="text/javascript" src="js/paho-mqtt.js" ></script>    
       <script type="text/javascript" src="js/js.js"></script>
       <script type="text/javascript" src="js/MQTTStatusInfo.js"></script>           
    </body>
</html>
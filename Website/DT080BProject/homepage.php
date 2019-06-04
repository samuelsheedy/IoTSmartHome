<!DOCTYPE html>
<?php
	session_start();;
?>
<html>
    <?php
        $tempDateArr = array();
        $tempRows = 0;
        $tempDateAr2r = array();
        $tempRows2 = 0;
        $tempDateArr3 = array();
        $tempRows3 = 0;
        $db_serverIPAddr = "127.0.0.1";           //IP address of the database 
        $db_serverUname = "Homer";                 //default username for MySQL
        $db_serverPwd = "Baron";                     //default password for MySQL
        $database = "smartHouseLongTerm";
        $i=0;
        
        $conn = mysqli_connect($db_serverIPAddr, $db_serverUname, $db_serverPwd, $database); //Open a connection to the MySQL database
        if (mysqli_connect_errno($conn)) //test the connection to the sql server and db
        {
            print("Error connecting to MySQL database" . mysqli_connect_error($conn));
        } 
        else
        {
            $stmt = ( "SELECT DISTINCT date FROM `airdataRoom1` WHERE 1"); //Prepare an SQL statement
            if ($result = mysqli_query($conn, $stmt)) 
            {
                $tempRows = mysqli_num_rows($result);
                while ($row = mysqli_fetch_assoc($result)) 
                {
                    $tempDateArr[$i]=$row["date"];
                    $i=$i+1;
                }
                $i=0;
            }	
            else
            {
            }
            $stmt = ( "SELECT DISTINCT date FROM `airdataRoom2` WHERE 1"); //Prepare an SQL statement
            if ($result = mysqli_query($conn, $stmt)) 
            {
                $tempRows2 = mysqli_num_rows($result);
                while ($row = mysqli_fetch_assoc($result)) 
                {
                    $tempDateArr2[$i]=$row["date"];
                    $i=$i+1;
                }
                $i=0;
            }	
            else
            {
            }
            $stmt = ( "SELECT DISTINCT date FROM `airdataRoom3` WHERE 1"); //Prepare an SQL statement
            if ($result = mysqli_query($conn, $stmt)) 
            {
                $tempRows3 = mysqli_num_rows($result);
                while ($row = mysqli_fetch_assoc($result)) 
                {
                    $tempDateArr3[$i]=$row["date"];
                    $i=$i+1;
                }
                $i=0;
            }	
            else
            {
            }
            mysqli_close($conn);
        }  
    ?>
    <head>
        <title>Data logger</title>
        <meta charset="UTF-8"> 
        <meta name="description" content="">
        <meta name="keywords" content="">
        
        <!--make website responsive for viewing on all devices-->
        <link rel="stylesheet" type="text/css" href="css/responsive.css" />
        
        <!--favicon image for title bar-->
		<link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
		<link rel="icon" href="images/favicon.ico" type="images/x-icon">
        
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" type="text/css" href="css/stylesheet.css">
        <link rel="stylesheet" type="text/css" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script type="text/javascript" src="https://code.jquery.com/jquery-1.12.4.js"></script>
        <script type="text/javascript" src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/1.20.2/TweenMax.min.js"></script>
        <script type="text/javascript" src="js/js.js"></script> 
        <script>
            $( function() 
            {
                $( "#datepickerTemp" ).datepicker({ dateFormat: 'yy-mm-dd', showButtonPanel: true, changeMonth: true, changeYear: true });
            } );
            $( function() {
                $( "#datepickerTemp2" ).datepicker({ dateFormat: 'yy-mm-dd', showButtonPanel: true, changeMonth: true, changeYear: true });
            } );
            $( function() {
                $( "#datepickerTemp3" ).datepicker({ dateFormat: 'yy-mm-dd', showButtonPanel: true, changeMonth: true, changeYear: true });
            } );
        </script>
    </head>
    
    <body id="homeBody">
        <div id="mySidenav" class="sidenav">
            <a href="javascript:void(0)" class="closebtn" onclick="closeNav()">&times;</a>
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
            <div id="main">
            <span style="font-size:30px;cursor:pointer; color:white;" onclick="openNav()">&#9776; TU Smart Home</span>
        </div>
        <div id="fadeDiv">
        </div>
        <div id="banner">
            <img id="logoImg" src="images/logo.png" alt="tu smart home logo">
            <div id="backImg">
            </div>
        </div>
        <div id="calContainer">
            <div class="calendar" id="calLeft">
                <form action="airTempChartRoom1.php" method="get">
                    <input class="calBox" type="text" value="Click here to select date" id="datepickerTemp" name="airTempDate" readonly>
                    <input class="selectButton" type="submit" value="Room One!" onclick="return testAirData()">
                </form>
                <p id="airError" class="errorText"></p>
                <img class="icon" alt="icon" src="images/thermom.png">
            </div>
            <div class="calendar" id="calCenter1">
                <form action="airTempChartRoom2.php" method="get">
                    <input class="calBox" type="text" value="Click here to select date" id="datepickerTemp2" name="airTempDate2" readonly>
                    <input class="selectButton" type="submit" value="Room Two!" onclick="return testAirData2()">
                </form>
                <p id="airError" class="errorText"></p>
                <img class="icon" alt="icon" src="images/thermom.png">
            </div>
            <div class="calendar" id="calRight">
                <form action="airTempChartRoom3.php" method="get">
                    <input class="calBox" type="text" value="Click here to select date" id="datepickerTemp3" name="airTempDate3" readonly>
                    <input class="selectButton" type="submit" value="Room Three!" onclick="return testAirData3()">
                </form>
                <p id="soilError" class="errorText"></p>
                <img class="icon" alt="icon" src="images/thermom.png">
            </div>  
        </div>
        <noscript>
            <div>
                <span style="color:red;">You will not be able to select a date, if you do not enable Javascript!!</span>
            </div>
        </noscript>
        <div id="spriteDiv">
            <img id="spriteflower" alt="flower" src="images/flower.svg">            
        </div>
        <script>
            function testAirData()
            {
                var jConvTempArray = <?php echo json_encode($tempDateArr); ?>;
                var tempRows= <?php echo json_encode($tempRows); ?>;
                var selectedDate = document.getElementsByName("airTempDate")[0].value;
                var i=0;
                for(i=0; i<tempRows; i++)
                {
                    if(selectedDate == jConvTempArray[i])
                    {
                        return true;
                    }
                }
                document.getElementById("airError").style.visibility = "visible";
                if(selectedDate != "Click here to select date")
                {
                    document.getElementById("airError").innerHTML = "No data for: " + selectedDate;
                }
                else
                {
                    document.getElementById("airError").innerHTML = "No date selected";
                }
                return false;
            }
            function testAirData2()
            {
                var jConvTempArray = <?php echo json_encode($tempDateArr2); ?>;
                var tempRows2= <?php echo json_encode($tempRows2); ?>;
                var selectedDate = document.getElementsByName("airTempDate2")[0].value;
                var i=0;
                for(i=0; i<tempRows2; i++)
                {
                    if(selectedDate == jConvTempArray[i])
                    {
                        return true;
                    }
                }
                document.getElementById("airError").style.visibility = "visible";
                if(selectedDate != "Click here to select date")
                {
                    document.getElementById("airError").innerHTML = "No data for: " + selectedDate;
                }
                else
                {
                    document.getElementById("airError").innerHTML = "No date selected";
                }
                return false;
            }
            function testAirData3()
            {
                var jConvSoilArray = <?php echo json_encode($tempDateArr3); ?>;
                var tempRows3 = <?php echo json_encode($tempRows3); ?>;
                var selectedDate = document.getElementsByName("airTempDate3")[0].value;
                var i=0;
                for(i=0; i<tempRows3; i++)
                {
                    if(selectedDate == jConvSoilArray[i])
                    {
                        return true;
                    }
                }
                document.getElementById("soilError").style.visibility = "visible";
                if(selectedDate != "Click here to select date")
                {
                    document.getElementById("soilError").innerHTML = "No data for: " + selectedDate;
                }
                else
                {
                    document.getElementById("soilError").innerHTML = "No date selected";
                }
                return false;
            }       
        </script>
    </body>
    
</html>

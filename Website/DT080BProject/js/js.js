function openNav() 
{
    if (window.matchMedia("(min-width: 500px)").matches) {
        /* the viewport is at least 500 pixels wide */
        document.getElementById("mySidenav").style.width = "10%";
        document.getElementById("mySidenav").style.height = "100%";
        document.getElementById("main").style.marginLeft = "10%";
      } else {
        /* the viewport is less than 500 pixels wide */
        document.getElementById("mySidenav").style.width = "100%";
        document.getElementById("mySidenav").style.height = "25%";
      }
}
function closeNav()
{
    document.getElementById("mySidenav").style.width = "0";
    document.getElementById("main").style.marginLeft= "0";
}
function openChartNav() {
    document.getElementById("mySidenav").style.width = "10%";
}

function closeChartNav() 
{
    document.getElementById("mySidenav").style.width = "0";
}




function activatePump()
{
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() 
    {
        if (this.readyState == 4 && this.status == 200)
        {
            document.getElementById("actPumpPara").style.color = "white"
            document.getElementById("actPumpPara").style.visibility ="visible"
            document.getElementById("actPumpPara").innerHTML = this.responseText;
            actPumpResponse();
        }
        else if (this.readyState == 1)
        {
            document.getElementById("actPumpPara").style.color = "white"
            document.getElementById("actPumpPara").style.visibility ="visible"
            document.getElementById("actPumpPara").innerHTML = "Processing Request";
        }
        else
        {
            document.getElementById("actPumpPara").style.visibility ="visible"
            document.getElementById("actPumpPara").style.color = "red"
            document.getElementById("actPumpPara").innerHTML = "Failed"
        }
    };
    xhttp.open("POST", "http://192.168.0.59/", true);
    xhttp.send("activatePump/");
}
function actPumpResponse()
{
    var stopRaining;
    document.getElementById("backImgActAnim").style.opacity = "100"
    document.getElementById("backImgActAnim").style.visibility = "visible"
    stopRaining = setTimeout(stopRainAnim, 15000); 
}
function stopRainAnim()
{
    document.getElementById("backImgActAnim").style.opacity = "0"
}

function login()
{
	var username = document.getElementById("userName").value;
	var password = document.getElementById("password").value;
    if(password == null || password == "" || username == null || username == "")
    {
        if((password == null || password == "") && (username == null || username == "") )
		{
			alert("Username and password were not entered");
			document.getElementById("userName").style.border="solid red 1px";
			document.getElementById("password").style.border="solid red 1px";
			document.getElementById("userName").value="";
			document.getElementById("password").value="";
			return false;
		}
		else if(username == null || username == "")
		{
			alert("Username not entered");
			document.getElementById("userName").style.border="solid red 1px";
			document.getElementById("password").style.border="solid green 1px";
			document.getElementById("userName").value="";
			return false;
		}
		else if(password == null || password == "")
		{
			alert("No password entered");
			document.getElementById("userName").style.border="solid green 1px";
			document.getElementById("password").style.border="solid red 1px";
			document.getElementById("password").value="";
			return false;
		}
		else
		{
			return true;
		}
    }
}

function registerS()
{
	var errCount=0;
	var errMessage = "Error: \n";
	var errUsername = "Username Not entered \n"
	var errPassword = "Password Not entered \n"
	var errName = "Name Not entered \n"
	var errEmail = "Email Not entered \n"
	var username = document.getElementById("username").value;
	var password = document.getElementById("password").value;
	var name = document.getElementById("name").value;
	var email = document.getElementById("email").value;
    if(password == null || password == "" || username == null || username == "" || name == null || name == "" || email == null || email == "")
    {
        if((password == null || password == "") && (username == null || username == "") && (name == null || name == "") && (email == null || email == "") )
		{
			alert("No fields were entered");
			document.getElementById("username").style.border="solid red 1px";
			document.getElementById("password").style.border="solid red 1px";
			document.getElementById("name").style.border="solid red 1px";
			document.getElementById("email").style.border="solid red 1px";
			return false;
		}
		if(username == null || username == "")
		{
			//alert("Username not entered");
			errMessage = errMessage.concat(errUsername);
			document.getElementById("username").style.border="solid red 1px";
			errCount++;
		}
		if(password == null || password == "")
		{
			//alert("No password entered");
			errMessage = errMessage.concat(errPassword);
			document.getElementById("password").style.border="solid red 1px";
			errCount++;
		}
		if(name == null || name == "")
		{
			errMessage = errMessage.concat(errName);
			//alert("Fullname not entered");
			document.getElementById("name").style.border="solid red 1px";
			errCount++;
		}
		if(email == null || email == "")
		{
			//alert("Email not entered");
			errMessage = errMessage.concat(errEmail);
			document.getElementById("email").style.border="solid red 1px";
			errCount++;
		}
		if(errCount!=0)
		{
			alert(errMessage);
			return false;
			
		}
		else
		{
			return true;
		}
    }
}
		
function loginBlank()
{
	document.getElementById("userName").style.border="solid grey 2px";	
}

function registerBlank()
{
	document.getElementById("username").style.border="solid grey 2px";	
}
	
function passwordBlank()
{
	document.getElementById("password").style.border="solid grey 2px";
}

function nameBlank()
{
	document.getElementById("name").style.border="solid grey 2px";
}

function eMailBlank()
{
	document.getElementById("email").style.border="solid grey 2px";
}

function errLoginCheck()
{
	var username = document.getElementById("userName").value;
	var password = document.getElementById("password").value;
	if(username == null || username == "")
	{
		document.getElementById("userName").style.border="solid red 1px";
	}
	if(password == null || password == "")
	{
		document.getElementById("password").style.border="solid red 1px";
	}
	
}

function errRegCheck()
{
	var username = document.getElementById("username").value;
	var password = document.getElementById("password").value;
	var name = document.getElementById("name").value;
	var email = document.getElementById("email").value;
	if(username == null || username == "")
	{
		//alert("Username not entered");
		document.getElementById("username").style.border="solid red 1px";
	}
	if(password == null || password == "")
	{
		document.getElementById("password").style.border="solid red 1px";
	}
	if(name == null || name == "")
	{
		document.getElementById("name").style.border="solid red 1px";
	}
	if(email == null || email == "")
	{
		document.getElementById("email").style.border="solid red 1px";
	}
	
}
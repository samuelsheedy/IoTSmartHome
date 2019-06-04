package project.temp.control;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

//This class implements the callback method for MQTT
public class MqttTest implements MqttCallback {

	//The variables needed to connect and subscribe 
	/*String topic        = "JavaTest"; //The topic that we are subscribing to
    int qos             = 2; //The quality of service we are using, not very important
    String broker       = "tcp://broker.mqttdashboard.com"; //The broker we are using (note probably should use local for final)
    String clientId     = "Room 1"; //The id of this client
    MemoryPersistence persistence = new MemoryPersistence();*/

    //print a message if we lose connection to the broker
    public void connectionLost(Throwable arg0) {
        System.err.println("connection lost");

    }
    //callback method when we successfully send a message
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        System.err.println("delivery complete");
    }

    //The callback method that will execute whenever there is an MQTT message received
    public void messageArrived(String topic, MqttMessage message) throws Exception {
	    String response = new String(message.getPayload());
	    System.out.println(response.substring(0, 3));
	    String rCode = response.substring(0, 3).trim();
	    if( rCode.equals("ALV")){ //Message format should be Code Room Number ie ALV: Room 1
	    	String roomNum = response.substring(10, 11);
	    	roomNum = roomNum.trim();
	    	System.out.println("Alive recieved from Room: "+roomNum);
		    int roomNumInt = Integer.parseInt(roomNum);
		    MaintainTempBDI.setRoomAliveArray(roomNumInt, true);
	    }
	    else if(rCode.equals("DED")){ //Message format should be Code Room Number ie ALV: Room 1
	    	String roomNum = response.substring(10, 11);
	    	roomNum = roomNum.trim();
	    	System.out.println("Dead recieved from Room: "+roomNum);
		    int roomNumInt = Integer.parseInt(roomNum);
		    MaintainTempBDI.setRoomAliveArray(roomNumInt, false);
	    }
	    else if(rCode.equals("TMP")){ //Message format should be Code Room Number Value ie TMP: Room 1 22
		    String roomNum = response.substring(10, 11);
		    String value = response.substring(12);
		    value = value.trim();
		    roomNum = roomNum.trim();
		    System.out.println("Temperature of "+value+" received from room "+roomNum);
		    //System.out.println(value);
		    int roomNumInt = Integer.parseInt(roomNum);
		    int valueInt = Integer.parseInt(value);
	        MaintainTempBDI.setTempArrays(roomNumInt, valueInt);
	    }
	    else if(rCode.equals("JRK")){ //Message format should be Code Room Number Value ie TMP: Room 1 22
		    String roomNum = response.substring(10, 11);
		    String value = response.substring(12);
		    value = value.trim();
		    roomNum = roomNum.trim();
		    System.out.println("Jerk of "+value+" received from room "+roomNum);
		    //System.out.println(value);
		    int roomNumInt = Integer.parseInt(roomNum);
		    float valueFloat = Float.parseFloat(value);
	        MaintainTempBDI.setJerkArrays(roomNumInt, valueFloat);
	    }
	    else if(rCode.equals("HTR")){
	    	String roomNum = response.substring(10, 11);
		    String value = response.substring(12);
		    value = value.trim();
		    roomNum = roomNum.trim();
		    System.out.println("Heater flag of "+value+" received from room "+roomNum);
		    //System.out.println(value);
		    int roomNumInt = Integer.parseInt(roomNum);
		    int heatVal = Integer.parseInt(value);
	        MaintainTempBDI.setHeat(roomNumInt, heatVal);
	    }
	    else if(rCode.equals("FAN")){
	    	String roomNum = response.substring(10, 11);
		    String value = response.substring(12);
		    value = value.trim();
		    roomNum = roomNum.trim();
		    System.out.println("Fan flag of "+value+" received from room "+roomNum);
		    //System.out.println(value);
		    int roomNumInt = Integer.parseInt(roomNum);
		    int fanVal = Integer.parseInt(value);
		    MaintainTempBDI.setFan(roomNumInt, fanVal);
	    }
	    else if(rCode.equals("BLD")){
	    	String roomNum = response.substring(10, 11);
		    String value = response.substring(12);
		    value = value.trim();
		    roomNum = roomNum.trim();
		    System.out.println("Blind flag of "+value+" received from room "+roomNum);
	    }
	    else if(rCode.equals("MAX")){
	    	String value = response.substring(5);
	    	value = value.trim();
	    	System.out.println("Max temperature: "+value);
	    	int maxTemp = Integer.parseInt(value);
	    	MaintainTempBDI.setMaxTemp(maxTemp);
	    }
	    else if(rCode.equals("MIN")){
	    	String value = response.substring(5);
	    	value = value.trim();
	    	System.out.println("Min temperature: "+value);
	    	int maxTemp = Integer.parseInt(value);
	    	MaintainTempBDI.setMinTemp(maxTemp);
	    }
	    else if(rCode.equals("ENB")){
	    	String roomNum = response.substring(10,11);
	    	String code = response.substring(12,15);
	    	String value = response.substring(16);
	    	value = value.trim();
	    	roomNum = roomNum.trim();
		    int roomNumInt = Integer.parseInt(roomNum);
		    int onOffVal = Integer.parseInt(value);
		    System.out.println("Room:" +roomNumInt);
		    System.out.println("onOff:" +onOffVal);
		    System.out.println("Code: "+code);
	    	if(code.equals("ALL")){
	    		MaintainTempBDI.setRoomStatus(roomNumInt, onOffVal);
	    	}
	    }
	    else if(rCode.equals("DTO")){
	    	String value = response.substring(5);
		    value = value.trim();
		    System.out.println("Device Timeout of "+value+" received");
		    //System.out.println(value);
		    long timeoutVal = Integer.parseInt(value);
		    MaintainTempBDI.setDeviceTimeout(timeoutVal);
	    }
	    
    }
    
}
package project.temp.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;
import com.pi4j.wiringpi.Spi;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;

@Agent
public class MaintainTempBDI{
	
	static //Mqtt variables and objects
	MqttAsyncClient client;
	static String topic = "JavaTest";
	static int qos      = 0;
	int maxInFlight		=32768;
	String broker       = "tcp://broker.mqttdashboard.com";
	String clientId     = "Room 3";
	MemoryPersistence persistence;// = new MemoryPersistence();
	final GpioController gpio = GpioFactory.getInstance();

	//SPI operations
    public static byte INIT_CMD = (byte) 0xD0;
    public static byte SPI_CHANNEL = 0x0;
    
    float accelOldTemp, accelNewTemp=0, oldTemp, newTemp=0; 
    float oldCloudCover=0, newCloudCover=0;
    float chanceOfRain = 0;
    long accelOldTime=0, oldTime=0; 
    long blindTimeout = 0, heaterTimeout = 0;
    long startTime = System.currentTimeMillis();
    long sunsetConverted = 0;
    long fanTimeout = 0;
    long startTimeout = System.currentTimeMillis();
    int heatFlag = 0, fanFlag = 0;
    int idealTempFlag=0;
    int devicesOff=0;
    int localFlag=0;
    
	static int blindFlag = 0;
    static int maxTemp = 20;
	static int minTemp = 18;
	static Integer[] roomStatusArray = new Integer[3];
	static long[] roomLastAlive = new long[3];
	static long deviceTimeout = 60000;
	//GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
    GpioPinDigitalMultipurpose mypin = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_06, PinMode.DIGITAL_OUTPUT);
    
    
	@AgentFeature
	protected IBDIAgentFeature bdiFeature;
	@AgentFeature
    protected IExecutionFeature execFeature;
	
	@Belief
	static boolean[] roomArray = new boolean[3]; //This array is used to see which rooms are operational
	@Belief
	float cloudCoverConverted; //The current cloud cover in the specified location
	@Belief
	float futureCloudCover; //The esitmated future cloud cover
	@Belief
	boolean sunIsUp; //Has the sun set
	@Belief
	protected static Integer[] tempArray = new Integer[3]; //This is to store the temperature in each room
	@Belief
	protected static float[] jerkArray = new float[3]; //This is to store the jerk of each room
	@Belief
	protected static Integer[] heatArrayFlag = new Integer[3]; //This is to store the heater status of each room
	@Belief
	protected static Integer[] fanArrayFlag = new Integer[3]; //This is to store the fan status of each room
	@Belief
	protected long currentTime; //what time does the program start at
	
	@AgentCreated
	public void init() throws IOException{
		//Set tempArray[2] to the current temp this is just for testing
		tempArray[2] = 0;
		roomStatusArray[2] = 1;
		for(int i=0; i<3;i++){
			heatArrayFlag[i] = 0;
			fanArrayFlag[i] = 0;
			roomLastAlive[i] = 0;	
		}
	}
	
	@Plan(trigger=@Trigger(goals=MaintainTempGoal.class))
	protected void openBlinds(){
		if(tempArray[2]<=minTemp && cloudCoverConverted < 50 && sunIsUp == true && (System.currentTimeMillis() - blindTimeout) > deviceTimeout){
			System.out.println("Plan 1");
			//True if it there is less than 50% cloud cover and it is not night time
			String state;
			TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:9E:EA"); //One of the adafruit devices, change this for each plan
			if(ttd.evaluateService()){
				if(ttd.discoverChars()){
					System.out.println("Tester found service");
					state = ttd.getState();
					System.out.println("Tester call: "+state);
					if(state.equals("closed")){
						//Device is off/closed open the blinds
						ttd.activateDevice("open");
						blindTimeout = System.currentTimeMillis();
						blindFlag = 1;
					}
					ttd.disconnectFromDev();
					ttd = null;
				}else{
					System.out.println("Error: could not find characteristics");
				}
			}else
			{
				System.out.println("Error: could not find service");
			}
		}
	}
	@Plan(trigger=@Trigger(goals=MaintainTempGoal.class))
	protected void startHeater(){
		boolean lowestTemp = false;
		if(roomArray[0] && roomArray[1]){
			//Find lowest temp between all 3 rooms
			if(tempArray[2]<tempArray[0] && tempArray[2] < tempArray[1]){
				lowestTemp = true;
			}else if(tempArray[2]==tempArray[0] || tempArray[2]==tempArray[1]){
				//Find the lowest jerk between all 3 rooms if the temperatures are the same
				if(jerkArray[2]>jerkArray[0] && jerkArray[2]>jerkArray[1]){
					lowestTemp=true;
				}
			}
		}else if (roomArray[0]){
			//Find lowest temp between room 1 and 3
			if(tempArray[2]<tempArray[0]){
				lowestTemp = true;
			}else if(tempArray[2]==tempArray[0]){
				//Find the highest jerk between room 1 and 3 if the temperatures are the same
				if(jerkArray[2]>jerkArray[0]){
					lowestTemp=true;
				}
			}
		}else if(roomArray[1]){
			//Find lowest temp between room 2 and 3
			if(tempArray[2]<tempArray[1]){
				lowestTemp = true;
			}else if(tempArray[2]==tempArray[1]){
				//Find the highest jerk between room 2 and 3 if the temperatures are the same
				if(jerkArray[2]>jerkArray[1]){
					lowestTemp=true;
				}
			}
		}else{
			//Only current room is alive
			lowestTemp = true;
		}
		if((System.currentTimeMillis() - blindTimeout) > deviceTimeout && heatArrayFlag[0]==0 && heatArrayFlag[1]==0){
			System.out.println("Plan 2");
			String state;
			TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:55:D2"); //One of the adafruit devices, change this for each plan
			if (localFlag ==0){
				localFlag=1;
				if(ttd.evaluateService()){
					if(ttd.discoverChars()){
						System.out.println("Tester found service");
						state = ttd.getState();
						System.out.println("Tester call: "+state);
						if(state.equals("open") && heatFlag == 0){
							//Device was already open/on at the begining
							ttd.activateDevice("close");
							heatArrayFlag[2]=0;
							heatFlag = 1;
						}else if(state.equals("open") && (System.currentTimeMillis() - heaterTimeout) >deviceTimeout){
							//Device is open/on for set amount of time
							ttd.activateDevice("close");
							heatArrayFlag[2]=0;
							heatFlag=1;
						}else if(state.equals("closed") && (System.currentTimeMillis() - heaterTimeout) >deviceTimeout && lowestTemp && tempArray[2]< minTemp){
							//Device is off/closed
							ttd.activateDevice("open");
							heaterTimeout = System.currentTimeMillis();
							heatArrayFlag[2]=1;
							heatFlag=1;
						}
						ttd.disconnectFromDev();
						ttd = null;
					}else{
						System.out.println("Error: could not find characteristics");
					}
				}else{
					System.out.println("Error: could not find service");
				}
			}
	
		}
	}
	@Plan(trigger=@Trigger(goals=MaintainTempGoal.class))
	protected void closeBlinds(){
		if(tempArray[2]>=maxTemp && sunIsUp == false && (System.currentTimeMillis() - blindTimeout) > deviceTimeout){
			System.out.println("Plan 3");
			//True if it is night time
			String state;
			TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:9E:EA"); //One of the adafruit devices, change this for each plan
			if(ttd.evaluateService()){
				if(ttd.discoverChars()){
					System.out.println("Tester found service");
					state = ttd.getState();
					System.out.println("Tester call: "+state);
					if(state.equals("open")){
						//Device is open/on close the blinds
						ttd.activateDevice("close");
						blindTimeout = System.currentTimeMillis();
						blindFlag = 0;
					}
					ttd.disconnectFromDev();
					ttd = null;
				}else{
					System.out.println("Error: could not find characteristics");
				}
			}else{
				System.out.println("Error: could not find service");
			}
			
		}
	}
	@Plan(trigger=@Trigger(goals=MaintainTempGoal.class))
	protected void startFan(){
		boolean highestTemp = false;	
		if(roomArray[0] && roomArray[1]){
			//Find highest temp between all 3 rooms
			if(tempArray[2]>tempArray[0] && tempArray[2]>tempArray[1]){
				highestTemp = true;
			}else if(tempArray[2]==tempArray[0] || tempArray[2]==tempArray[1]){
				//Find the highest jerk between all 3 rooms if the temperatures are the same
				if(jerkArray[2]>jerkArray[0] && jerkArray[2]>jerkArray[1]){
					highestTemp=true;
				}
			}
		}else if (roomArray[0]){
			//Find highest temp between room 1 and 3
			if(tempArray[2]>tempArray[0]){
				highestTemp = true;
			}else if(tempArray[2]==tempArray[0]){
				//Find the highest jerk between room 1 and 3 if the temperatures are the same
				if(jerkArray[2]>jerkArray[0]){
					highestTemp=true;
				}
			}
		}else if(roomArray[1]){
			//Find highest temp between room 2 and 3
			if(tempArray[2]>tempArray[1]){
				highestTemp = true;
			}else if(tempArray[2]==tempArray[1]){
				//Find the highest jerk between room 2 and 3 if the temperatures are the same
				if(jerkArray[2]>jerkArray[1]){
					highestTemp=true;
				}
			}
		}else{
			//Only current room is alive
			highestTemp = true;
		}
		
		if((System.currentTimeMillis()-fanTimeout)>deviceTimeout){
		    if((System.currentTimeMillis()-blindTimeout)>deviceTimeout && highestTemp && tempArray[2]>maxTemp && heatArrayFlag[0]==0 && heatArrayFlag[1]==0){
				System.out.println("Plan 4");
				mypin.high();
				fanArrayFlag[2]=1;
				fanTimeout = System.currentTimeMillis();
			}else if(heatArrayFlag[2]==1){
				fanArrayFlag[2]=0;
				mypin.low();
			}
		}
	}
	
	@Plan(trigger=@Trigger(goals=MaintainTempGoal.class))
	protected void closeBlindsCloud(){
		if(futureCloudCover > 50 && (System.currentTimeMillis() - blindTimeout) > deviceTimeout){
			System.out.println("Plan 5");
			//True if it is going to be cloudy in 15 minutes
			String state;
			TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:9E:EA"); //One of the adafruit devices, change this for each plan
			if(ttd.evaluateService()){
				if(ttd.discoverChars()){
					System.out.println("Tester found service");
					state = ttd.getState();
					System.out.println("Tester call: "+state);
					if(state.equals("open")){
						//If the blinds are open close them
						 ttd.activateDevice("close");
						 blindFlag = 0;
					}else if(state.equals("closed")){
						//If the blinds are already closed do nothing
						//blindTimeout = System.currentTimeMillis();
					}
					ttd.disconnectFromDev();
					ttd = null;
				}else{
					System.out.println("Error: could not find characteristics");
				}
			}else{
				System.out.println("Error: could not find service");
			}
		}
	}
	@Plan(trigger=@Trigger(goals=MaintainTempGoal.class))
	protected void openBlindsCloud(){
		if(futureCloudCover < 50 && (System.currentTimeMillis() - blindTimeout) > deviceTimeout){
			System.out.println("Plan 6");
			//True if it is going to be cloudy in 15 minutes
			String state;
			TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:9E:EA"); //One of the adafruit devices, change this for each plan
			if(ttd.evaluateService()){
				if(ttd.discoverChars()){
					System.out.println("Tester found service");
					state = ttd.getState();
					System.out.println("Tester call: "+state);
					if(state.equals("closed")){
						//If the blinds are closed open them
						 ttd.activateDevice("open");
						 blindFlag = 1;
					}else if(state.equals("open")){
						//If the blinds are already open do nothing
						//blindTimeout = System.currentTimeMillis();
					}
					ttd.disconnectFromDev();
					ttd = null;
				}else{
					System.out.println("Error: could not find characteristics");
				}
			}else{
				System.out.println("Error: could not find service");
			}
		}
	}
	
	@AgentBody
	public void body(){
		//call the function to connect to the mqtt broker
		conToBroker();
		//Every 1 minutes check the acceleration and calculate the jerk
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				//start our goals if the room is enabled and the timeout
				if(roomStatusArray[2]==1 && (System.currentTimeMillis() - startTimeout)>12000){
					localFlag=0;
					bdiFeature.dispatchTopLevelGoal(new MaintainTempGoal());
					if((idealTempFlag==1 && heatArrayFlag[0]==1)||(idealTempFlag==1 && fanArrayFlag[0]==1)){
						if((System.currentTimeMillis() - heaterTimeout) >deviceTimeout){
							turnOffHeater();
						}
						if((System.currentTimeMillis()-fanTimeout)>deviceTimeout){
							turnOffFan();
						}
					}
				}
				return IFuture.DONE;
			}

		});
		//Every 10 minutes check the acceleration and calculate the jerk
		execFeature.repeatStep(0, 600000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
			  	accelOldTemp = accelNewTemp;
			  	oldTemp=newTemp;
			  	newTemp = tempArray[2];
			  	accelNewTemp = (newTemp - oldTemp)/(System.currentTimeMillis() - oldTime);
			  	oldTime = System.currentTimeMillis();
			  	jerkArray[2] = (accelNewTemp - accelOldTemp)/(System.currentTimeMillis() - accelOldTime);
			  	accelOldTime = System.currentTimeMillis();
			  	//System.out.println("New Accel Value:" +accelNewTemp);
			  	//System.out.println("Jerk:" +jerkArray[2]);
			  	return IFuture.DONE;
			}
		});  
		//Every 10 minutes check whether the sun has risen or set and check the current cloud cover
		execFeature.repeatStep(0, 600000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				String location = "Dublin";
		        try{
		        	//We are using the darksky api
		        	URL url = new URL("https://api.darksky.net/forecast/c9d78ec65acd4f66bd5ef70fc23491a4/53.337085,-6.267336");
		            HttpURLConnection con = (HttpURLConnection) url.openConnection();
		            con.setInstanceFollowRedirects(false);
		            con.setRequestMethod("GET");
		            con.setRequestProperty("Content-Type", "application/json");
		            //String contentType = con.getHeaderField("Content-Type");
		            con.setConnectTimeout(5000);
		            con.setReadTimeout(5000);

		            //int status = con.getResponseCode();
		            //Read the response from the server into a buffer
		            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		            String inputLine;
		            //Store the buffer in a StringBuilder object
		            StringBuilder data = new StringBuilder();
		            while ((inputLine = in.readLine()) != null) {
		                data.append(inputLine);
		            }
		            in.close();
		            con.disconnect();
		            //Convert the StringBuilder to a string the string is formatted as Json which should make it easier to parse
		            String json = data.toString();
		            System.out.println(json);

		            //This uses the Gson library to parse the json data sent from DarkSky for the current cloud coverage in the selected city
		            JsonElement jE = new JsonParser().parse(json);
		            JsonObject  jOb = jE.getAsJsonObject();
		            jOb = jOb.getAsJsonObject("currently");
		            String cloudCoverStr = jOb.get("cloudCover").toString();
		            System.out.println(cloudCoverStr);            //Remember if you want to get other fields of the json input then if they are contained within [] brackets you will have to add a JsonArray object as well
		            //Convert the cloud cover value into a factor of 100
		            cloudCoverConverted = Float.parseFloat(cloudCoverStr)*100;
		            System.out.printf("There is currently %.2f %% cloud cover in %s\n", cloudCoverConverted, location);
		            
		            //Get the time of sunrise in the selected location
		            jOb = jE.getAsJsonObject();
		            jOb = jOb.getAsJsonObject("daily");
		            JsonArray jA = jOb.getAsJsonArray("data");
		            jOb = jA.get(0).getAsJsonObject();
		            String sunriseStr = jOb.get("sunriseTime").toString();
		            System.out.println(sunriseStr);
		            //Convert the sunrise string value
		            long sunriseConverted = Integer.parseInt(sunriseStr);
		            System.out.printf("Sunrise is at %d in %s%n", sunriseConverted, location);
		            
		            //get the time of sunset in the selected location
		            jOb = jE.getAsJsonObject();
		            jOb = jOb.getAsJsonObject("daily");
		            jA = jOb.getAsJsonArray("data");
		            jOb = jA.get(0).getAsJsonObject();
		            String sunsetStr = jOb.get("sunsetTime").toString();
		            System.out.println(sunsetStr);
		            //Convert the sunset string value
		            sunsetConverted = Integer.parseInt(sunsetStr);
		            System.out.printf("Sunset is at %d in %s%n", sunsetConverted, location);
		            
		            jOb = jE.getAsJsonObject();
		            jOb = jOb.getAsJsonObject("minutely");
		            jA = jOb.getAsJsonArray("data");
		            jOb = jA.get(15).getAsJsonObject();
		            String chanceOfRainStr = jOb.get("precipProbability").toString();
		            System.out.println(chanceOfRainStr);
		            //Convet the chance of rain sreing value
				    chanceOfRain = Float.parseFloat(chanceOfRainStr);
				    System.out.printf("The chance of rain is: "+chanceOfRain);
		            
		            //collect the garbage
		            jOb = null;
		            jA = null;
		            jE = null;
		            json = null;
		            
		            long currentTime = System.currentTimeMillis()/1000; //We need to divide by 1000 because dark sky api returns a 10 character unix timestamp while java uses 13 characters
		            System.out.printf("Current time is at %d in %s%n", currentTime, location);
		            
		            //test if the time is between sunrise and sunset
		            if(currentTime > sunriseConverted && currentTime < sunsetConverted){
		            	sunIsUp = true;
		                System.out.println("Sun is up");
		            }else{
		            	sunIsUp = false;
		                System.out.println("Sun has set");
		            }    
	        	}
		        catch(MalformedURLException | ProtocolException e){
		        }
		        catch(IOException | JsonSyntaxException | NumberFormatException e1){
		        }
		        return IFuture.DONE;	
			}
		});
		//Every 10 minutes check the acceleration and calculate the jerk
		execFeature.repeatStep(0, 600000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				oldCloudCover = newCloudCover;
			  	newCloudCover = cloudCoverConverted;
			  	futureCloudCover = cloudCoverConverted + (newCloudCover - oldCloudCover);
			  	System.out.println("Future cloud cover: "+futureCloudCover);
			  	return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				try{
					//int temp = 0; 
					//java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
					//java.sql.Time time = new java.sql.Time(System.currentTimeMillis());
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1/projecttest","Homer","Baron");
					//Homer is username, Baron is the password & Project is the database
					PreparedStatement stmt = con.prepareStatement("SELECT temp FROM tempdata WHERE counter = ?");
					stmt.setInt(1, 1);
					ResultSet result = stmt.executeQuery();
					while(result.next()){
						tempArray[2] = result.getInt("temp");
						System.out.println("Test");
					}
					System.out.println("Current temp ="+ tempArray[2]);
					con.close();
					stmt.close();
				}catch(Exception e){
					System.out.print(e);
				}
				System.out.println(tempArray[2]);
				//tempArray[2] = read();
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 30000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() != true){
					conToBroker();
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 30000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() && roomStatusArray[2]==1){
					String aliveMessage = "ALV: Room 3";
					MqttMessage message = new MqttMessage(aliveMessage.getBytes());
					message.setQos(qos);
					try {
						client.publish(topic, message);
					}catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() && roomStatusArray[2]==1){
					String tempMessage = "TMP: Room 3 ";
					tempMessage = tempMessage += tempArray[2];
					MqttMessage tempMQMessage = new MqttMessage(tempMessage.getBytes());
					tempMQMessage.setQos(qos);
					try {
						client.publish(topic, tempMQMessage);
					}catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() && roomStatusArray[2]==1){
					String jerkMessage = "JRK: Room 3 ";
					jerkMessage = jerkMessage += jerkArray[2];
					MqttMessage jerkMQMessage = new MqttMessage(jerkMessage.getBytes());
					jerkMQMessage.setQos(qos);
					try {
	    			 client.publish(topic, jerkMQMessage);
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() && roomStatusArray[2]==1){
					String heatMessage = "HTR: Room 3 ";
					heatMessage = heatMessage += heatArrayFlag[2];
					MqttMessage heatMQMessage = new MqttMessage(heatMessage.getBytes());
					heatMQMessage.setQos(qos);
					try {
						client.publish(topic, heatMQMessage);
					}catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() && roomStatusArray[2]==1){
					String fanMessage = "FAN: Room 3 ";
					fanMessage = fanMessage += fanArrayFlag[2];
					MqttMessage fanMQMessage = new MqttMessage(fanMessage.getBytes());
					fanMQMessage.setQos(qos);
					try {
						client.publish(topic, fanMQMessage);
					}catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 60000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(client.isConnected() && roomStatusArray[2]==1){
					String blindMessage = "BLD: Room 3 ";
					blindMessage = blindMessage += blindFlag;
					MqttMessage blindMQMessage = new MqttMessage(blindMessage.getBytes());
					blindMQMessage.setQos(qos);
					try {
						client.publish(topic, blindMQMessage);
					}catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 900000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
			if((sunsetConverted - (System.currentTimeMillis()/1000)) < 900 && (System.currentTimeMillis() - blindTimeout) > deviceTimeout){
				System.out.println("Plan 5");
				//True if it is 15 minutes before sunset
				String state;
				TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:9E:EA"); //One of the adafruit devices, change this for each plan
				if(ttd.evaluateService()){
					if(ttd.discoverChars()){
						System.out.println("Tester found service");
						state = ttd.getState();
						System.out.println("Tester call: "+state);	
						if(state.equals("open")){
							//If the blinds are open close them
							 ttd.activateDevice("close");
							 blindFlag = 0;
						}else if(state.equals("closed")){
							//If the blinds are already closed do nothing
							//blindTimeout = System.currentTimeMillis();
						}
						ttd.disconnectFromDev();
						ttd = null;
					}else{
						System.out.println("Error: could not find characteristics");
					}
				}else{
					System.out.println("Error: could not find service");
				}
				
			}
			return IFuture.DONE;
			}
		});
		execFeature.repeatStep(0, 120000, new IComponentStep<Void>(){
			public IFuture<Void> execute(IInternalAccess ia){
				if(roomLastAlive[0]-System.currentTimeMillis() > 120000){
					roomArray[0]=false;
					heatArrayFlag[0]=0;
					fanArrayFlag[0]=0;
				}else if(roomLastAlive[1]-System.currentTimeMillis() > 120000){
					roomArray[1]=false;
					heatArrayFlag[1]=0;
					fanArrayFlag[1]=0;
				}			
				return IFuture.DONE;
			}
		});
		
	}
	
	@Goal//(recur = true)
	public class MaintainTempGoal{
		@GoalMaintainCondition(beliefs="tempArray")
		protected boolean maintain(){
			if(tempArray[2] <=minTemp || tempArray[2] >=maxTemp){
				idealTempFlag =0;
				devicesOff =0;
				return false;
			}else{
				idealTempFlag =1;
				return true;
			}
		}  	  
	}
	
	public void turnOffHeater(){
		if (devicesOff ==0){
			System.out.println("Turning off devices");
			String state;
			TalkToDevices ttd = new TalkToDevices("30:AE:A4:C5:55:D2"); //One of the adafruit devices, change this for each plan
				if(ttd.evaluateService()){
					if(ttd.discoverChars()){
						System.out.println("Tester found service");
						state = ttd.getState();
						System.out.println("Tester call: "+state);
						if(state.equals("open")){
							//Device was already open/on at the begining
							ttd.activateDevice("close");
							heatArrayFlag[2]=0;
						}
						ttd.disconnectFromDev();
						ttd = null;
					}else{
						System.out.println("Error: could not find characteristics");
					}
				}else{
					System.out.println("Error: could not find service");
				}
			}
		}
	public void turnOffFan(){
		if (devicesOff ==0){
			mypin.low();
			fanArrayFlag[2]= 0;
			devicesOff = 1;
		}
	}
	
	public static void setTempArrays(int roomNum, int val) {
		tempArray[roomNum-1] = val;
	}
	public static void setJerkArrays(int roomNum, float floatVal){
		jerkArray[roomNum-1] = floatVal;
	}
	public static void setRoomAliveArray(int roomNum, boolean status) {
        roomArray[roomNum-1] = status;
	}
	public static void setHeat(int roomNum, int heatVal){
		heatArrayFlag[roomNum-1] = heatVal;
	}
	public static void setFan(int roomNum, int fanVal){
		fanArrayFlag[roomNum-1] = fanVal;
	}
	public static void setMaxTemp(int mTemp){
		maxTemp = mTemp;
	}
	public static void setMinTemp(int mTemp){
		minTemp = mTemp;
	}
	public static void setDeviceTimeout(long val){
		deviceTimeout = 60000*val;
	}
	public static void setRoomStatus(int roomNum, int status){
		roomStatusArray[roomNum-1] = status;
		if(client.isConnected() && roomStatusArray[2]==0){
			String aliveMessage = "DED: Room 3";
			MqttMessage message = new MqttMessage(aliveMessage.getBytes());
			message.setQos(qos);
			try {
				client.publish(topic, message);
			}catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	//this method is being used to connect or reconnect to the mqtt broker
	//it also sets the class that contains the receive message handler
	public void conToBroker(){
		try {
			persistence = new MemoryPersistence();
            client = new MqttAsyncClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(120);
            connOpts.setConnectionTimeout(60);
            System.out.print("Timeout: "+connOpts.getConnectionTimeout());
            connOpts.setMaxInflight(maxInFlight);
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            client.setCallback(new MqttTest());
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected");
            Thread.sleep(1000);
            client.subscribe(topic, qos);
            System.out.println("Subscribed");
        }catch (Exception me) {
        	if(me instanceof MqttException) {
        		System.out.println("reason " + ((MqttException) me).getReasonCode());
            }
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
	
	public void connectionLost(Throwable arg0) {
	        System.err.println("connection lost");
	}
	
	public void deliveryComplete(IMqttDeliveryToken arg0) {
	    System.err.println("delivery complete");
	}
	
	public static int read(){      
        //Send test ASCII message
        byte packet[] = new byte[2];
        packet[0] = INIT_CMD;  // address byte
        //packet[0] = (byte)(INIT_CMD | (SPI_CHANNEL<<5));
        packet[1] = 0x00;  // dummy
           
        Spi.wiringPiSPIDataRW(SPI_CHANNEL, packet, 2);        

        int dec = Integer.parseInt(bytesToHex(packet),16);
        dec = mapVal(dec);
        System.out.println(dec);
        return dec;
    }
	
    public static int mapVal(int val){
		val = val/50;
    	return val;
    }
    
    public static String bytesToHex(byte[] bytes) {
    	final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for( int j = 0; j < bytes.length; j++ ){
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

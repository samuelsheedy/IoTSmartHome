package project.temp.control;

import tinyb.*;

import java.time.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

public class TalkToDevices {
	private boolean discoveryStarted;
	private String address; 
    private BluetoothManager manager = BluetoothManager.getBluetoothManager();
    private BluetoothDevice sensor;
    private BluetoothGattCharacteristic devCharacteristic;
    BluetoothGattService devService;
    private final Lock lock; 
    private final Condition cv;
    
    public TalkToDevices(String add){
    	this.address = add;
        lock = new ReentrantLock();
        cv = lock.newCondition();
    }

    public boolean evaluateService()
    {
    	discoveryStarted = manager.startDiscovery();
        System.out.println("The discovery started: " + (discoveryStarted ? "true" : "false"));
        sensor = manager.find(null, address, null, Duration.ofSeconds(30));
        
        try {
            manager.stopDiscovery();
        } catch (BluetoothException e) {
            System.err.println("Discovery could not be stopped right now");
        }

        if (sensor == null) {
            System.err.println("No sensor found with the provided address.");
           // System.exit(-1);
        }

        System.out.print("Found device: ");
        printDevice(sensor);

        if (sensor.connect()){
            System.out.println("Sensor with the provided address connected");
        	return true;
		}
        else {
            System.out.println("Could not connect device.");
            return false;
            //System.exit(-1);
        }
    }

    public boolean discoverChars()
    {
	    devService = sensor.find("da417395-f1ed-4da2-8d26-b8c4d0117564");
	
	    if (devService == null) {
	        System.err.println("Service not found...");
	        sensor.disconnect();
	        return false;
	    }
	    System.out.println("Found service " + devService.getUUID());
	    System.out.println("looking for characteristics");
	    devCharacteristic = devService.find("da417396-f1ed-4da2-8d26-b8c4d0117564");
	    
	    if (devCharacteristic == null) {
	        System.err.println("Could not find the correct characteristics. Disconnecting");
	        sensor.disconnect();
	        return false;
	    }
	    System.out.println("Found the temperature characteristics");
	    return true;
    }

	public void printDevice(BluetoothDevice device) {
		System.out.print("Address = " + device.getAddress());
		System.out.print(" Name = " + device.getName());
		System.out.print(" Connected = " + device.getConnected());
		System.out.println();
    }


	public String getState()
	{
		byte[] response = devCharacteristic.readValue();
	    String state = new String(response);
        System.out.println(state);
        lock.lock();
        try {
            cv.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
            lock.unlock();
        }
	    //sensor.disconnect();
	    //System.out.println("disconnected");
        return state;
    }  
	
	public void activateDevice(String strToSend)
	{
    	byte[] config = strToSend.getBytes();
        devCharacteristic.writeValue(config);

        lock.lock();
        try {
            cv.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
            lock.unlock();
        }
	    sensor.disconnect();
    }
	public void disconnectFromDev()
	{
		sensor.disconnect();
	    System.out.println("disconnected");

	}
        
}
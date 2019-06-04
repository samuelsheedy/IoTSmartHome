/*
    https://github.com/nkolban/esp32-snippets/blob/master/Documentation/BLE%20C%2B%2B%20Guide.pdf 
*/
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// define the UUIDs and associate them with a value
// because of how the code operates all of our devices 
// can use the same UUID
#define SERVICE_UUID        "da417395-f1ed-4da2-8d26-b8c4d0117564"
#define CHARACTERISTIC_UUID "da417396-f1ed-4da2-8d26-b8c4d0117564"

//Create the class that will handle the read and write callbacks
class RWCallbackHandler: public BLECharacteristicCallbacks {
  //if there is an attempt to read the value from the characteristic
  //then run this handler
   void onRead(BLECharacteristic *pCharacteristic) {
      String convToString = "";
      Serial.println("Read");
      //If the current state of the output pin is HIGH
      //then write open to the characteristic
      if(digitalRead(33) == HIGH)
      {
         pCharacteristic->setValue("open");
      }
      //If it is low then set the characteristic value to closed
      if(digitalRead(33) == LOW)
      {
         pCharacteristic->setValue("closed");
      }
    }
    //This is the handler for a write characteristic
    void onWrite(BLECharacteristic *pCharacteristic) {
      String convToString = "";
      //get the value that was written to the characteristic
      std::string value = pCharacteristic->getValue();
      Serial.println("Write");
      //The characteristic value is a char array
      //so we need to parse each byte and concatenate 
      //it to a String
      if(value.length() > 0) {
        for (int i = 0; i < value.length(); i++){
          convToString += value[i];
        }
        Serial.println(convToString);
        //if the write was to open
        //then turn on the device
        if(convToString == "open"){
            //turn on the heater
            digitalWrite(33,HIGH);
        }
        else if(convToString == "close")
        {
            //turn off the heater
            digitalWrite(33,LOW);
        }
      }
    }
};

class ConStateCallbacks: public BLEServerCallbacks {
    //When a device connects this function is called
    void onConnect(BLEServer* pServer, BLECharacteristic *pCharacteristic,BLEAdvertising *pAdvertising) {
      //stop advertising while we are connected to a device
      pAdvertising->setScanResponse(false);
      pAdvertising->stop();
      //check the pins and set the characteristic value
      if(digitalRead(33) == HIGH)
      {
         pCharacteristic->setValue("open");
      }
      if(digitalRead(33) == LOW)
      {
         pCharacteristic->setValue("closed");
      }
    };
    //on a disconnect restart advertisements 
    void onDisconnect(BLEServer* pServer, BLECharacteristic *pCharacteristic,BLEAdvertising *pAdvertising) {
      //deviceConnected = false;
  
      pAdvertising->setScanResponse(true);
      pAdvertising->start();
      //check the pins and set the characteristic value
      if(digitalRead(33) == HIGH)
      {
         pCharacteristic->setValue("open");
      }
      if(digitalRead(33) == LOW)
      {
         pCharacteristic->setValue("closed");
      }
    }
};

void setup() {
  //The initialisation code for the system
  Serial.begin(115200);
  
  //set the pin to output and ensure it is disabled on startup
  pinMode(33, OUTPUT);
  digitalWrite(33,LOW);

  //Assign the identifier for the particular device
  BLEDevice::init("Room 3: Heater");
  //create the server object
  BLEServer *pServer = BLEDevice::createServer();

  //assign the services to the server
  BLEService *pService = pServer->createService(SERVICE_UUID);

  //assign the characteristics to the service, give it the read and write 
  //properties
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_READ
                                       );
  //Assign the callbacks for the characteristic
  pCharacteristic->setCallbacks(new RWCallbackHandler());
  //set the initial characterisctic value to closed
  pCharacteristic->setValue("closed");

  pService->start();

  //Begin advertising the services
  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->setScanResponse(true);
  pAdvertising->start();

  //assign the connection state callbacks
  pServer->setCallbacks(new ConStateCallbacks());
}

void loop() {
  //delay(2000);
}

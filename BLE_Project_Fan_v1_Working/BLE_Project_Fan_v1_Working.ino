/*
    https://github.com/nkolban/esp32-snippets/blob/master/Documentation/BLE%20C%2B%2B%20Guide.pdf 
*/
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

#define SERVICE_UUID        "da417395-f1ed-4da2-8d26-b8c4d0117564"
#define CHARACTERISTIC_UUID "da417396-f1ed-4da2-8d26-b8c4d0117564"

class RWCallbackHandler: public BLECharacteristicCallbacks {
   void onRead(BLECharacteristic *pCharacteristic) {
      String convToString = "";
      Serial.println("Read");
      if(digitalRead(33) == HIGH)
      {
         pCharacteristic->setValue("open");
      }
      if(digitalRead(33) == LOW)
      {
         pCharacteristic->setValue("closed");
      }
      //std::string value = pCharacteristicTwo->getValue();
    }
    void onWrite(BLECharacteristic *pCharacteristic) {
      String convToString = "";
      std::string value = pCharacteristic->getValue();
      Serial.println("Write");
      if(value.length() > 0) {
        for (int i = 0; i < value.length(); i++){
          convToString += value[i];
        }
        Serial.println(convToString);
        if(convToString == "open"){
            //turn on the heater
            digitalWrite(15,HIGH);
        }
        else if(convToString == "close")
        {
            //turn off the heater
            digitalWrite(15,LOW);
        }
      }
    }
};

class ConStateCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer, BLECharacteristic *pCharacteristic) {
      if(digitalRead(33) == HIGH)
      {
         pCharacteristic->setValue("open");
      }
      if(digitalRead(33) == LOW)
      {
         pCharacteristic->setValue("closed");
      }
    };
    void onDisconnect(BLEServer* pServer, BLECharacteristic *pCharacteristic) {
      //deviceConnected = false;
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
  Serial.begin(115200);
  
  pinMode(33, OUTPUT);
  //test the speed of the motor
  Serial.println("Success");
  BLEDevice::init("Room 1: Fan");
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(SERVICE_UUID);

  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_READ
                                       );

  pCharacteristic->setCallbacks(new RWCallbackHandler());

  digitalWrite(33,LOW);
  pCharacteristic->setValue("closed");

  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->setScanResponse(true);
  pAdvertising->start();

  pServer->setCallbacks(new ConStateCallbacks());
}

void loop() {
  // put your main code here, to run repeatedly:
  delay(2000);
}

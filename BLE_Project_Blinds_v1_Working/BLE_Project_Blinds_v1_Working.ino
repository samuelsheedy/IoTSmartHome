
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <Stepper.h>


#define SERVICE_UUID        "da417395-f1ed-4da2-8d26-b8c4d0117564"
#define BLIND_CHARACTERISTIC_UUID "da417396-f1ed-4da2-8d26-b8c4d0117564"
#define SWITCH_CHARACTERISTIC_UUID "da417397-f1ed-4da2-8d26-b8c4d0117564"

//Steps for each revolution = 360/step angle
//for the MY5602 the step angle is 1.8
//so 360/1.8 = 200
int stepsEachRev = 200;

//A+: Black, A-: Green, B+: Red, B-: Blue
//Center A: Yellow, Center B: White
Stepper MY5602(stepsEachRev, 5,18,19,21);

class WriteCallback: public BLECharacteristicCallbacks {
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
          //Open the blinds
          //need to check the switch to 
          //see if the blinds are already open
          while(digitalRead(33) == LOW){
            Serial.println(digitalRead(33));
            MY5602.step(1);
          }
          Serial.println(digitalRead(3));
        }
        else if(convToString == "close")
        {
          //close the blinds
          //remember to check if the blinds are already
          //closed
          while(digitalRead(33) == HIGH){
            Serial.println(digitalRead(33));
            MY5602.step(-1);
          }
        }
      }
    }
};

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      //deviceConnected = true;
      //pAdvertising->stop();
    };
    void onDisconnect(BLEServer* pServer, BLECharacteristic *pCharacteristic) {
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
  
  pinMode(33, INPUT_PULLUP);
  //test the speed of the motor
  MY5602.setSpeed(20);
  Serial.println("Success");
  BLEDevice::init("Room 2: Blinds");
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(SERVICE_UUID);

  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         BLIND_CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_READ
                                       );

  pCharacteristic->setCallbacks(new WriteCallback());
  if(digitalRead(33) == HIGH)
  {
    pCharacteristic->setValue("open");   
  }
  else{
    pCharacteristic->setValue("closed");
  }

  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->setScanResponse(true);
  pAdvertising->start();

  pServer->setCallbacks(new MyServerCallbacks());
}

void loop() {

}

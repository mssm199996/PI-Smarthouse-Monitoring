#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include "WebSocketsClient.h"

char AP_SSID[] = "Mssm network";
char AP_PASSWORD[] = "mssm1996";

char DEVICE_SERIAL_NUMBER[] = "DFSE65FR498F3DSF6";
char ROOM_NAME[] = "La cuisine";

char WEBSOCKET_SERVER_HOST[] = "192.168.43.189";
int WEBSOCKET_SERVER_PORT = 4567;

long LAST_TEMPERATURE_SNAPSHOT = 0;
long TEMPERATURE_SNAPSHOT_PERIOD = 1000;

byte WIFI_AP_LED_CONTROLLER = D0;
byte WEBSOCKET_DISCOVERY_LED_CONTROLLER = D4;
byte WEBSOCKET_TEMPERATURE_LED_CONTROLLER = D5;

WebSocketsClient discoveryWebSocketClient;
WebSocketsClient temperatureWebSocketClient;

boolean CONNECTED_TO_WIFI = false;
boolean CONNECTED_TO_DISCOVERY_WEBSOCKET = false;
boolean CONNECTED_TO_TEMPERATURE_WEBSOCKET = false;

boolean IDENTIFIED_TO_DISCOVERY_WEBSOCKET = false;

// ---------------------------------------------------------------------------------------------------------

void WifiAPHandling();
void WifiClientHandling();

void SendDiscoveryMessage();
void SendTemperatureValue();

void onDiscoveryEvent(WStype_t type, uint8_t * payload, size_t length);
void onTemperatureEvent(WStype_t type, uint8_t * payload, size_t length);

void buzzer();
// ---------------------------------------------------------------------------------------------------------

void setup() {
  Serial.begin(9600);

  pinMode(WIFI_AP_LED_CONTROLLER, OUTPUT);
  pinMode(WEBSOCKET_DISCOVERY_LED_CONTROLLER, OUTPUT);
  pinMode(WEBSOCKET_TEMPERATURE_LED_CONTROLLER, OUTPUT);

  randomSeed(5);
}

void loop() {
  if(!CONNECTED_TO_WIFI) {  
    WifiAPHandling();
    WifiClientHandling();
  }

  if(CONNECTED_TO_DISCOVERY_WEBSOCKET && !IDENTIFIED_TO_DISCOVERY_WEBSOCKET) {
    SendDiscoveryMessage();
  }

  if(CONNECTED_TO_TEMPERATURE_WEBSOCKET) {
    SendTemperatureValue();
  }

  // -------------------------------------------------------------------------

  discoveryWebSocketClient.loop();
  temperatureWebSocketClient.loop();

  discoveryWebSocketClient.sendPing();
  temperatureWebSocketClient.sendPing();
}

// ------------------------------------------------------------------------------------------------------------
// ------------------------------------------------------------------------------------------------------------

void WifiAPHandling() {
  CONNECTED_TO_DISCOVERY_WEBSOCKET = false;

  digitalWrite(WIFI_AP_LED_CONTROLLER, LOW);
  digitalWrite(WEBSOCKET_DISCOVERY_LED_CONTROLLER, LOW);

  Serial.println("Connecting to access points... please wait...");

  WiFi.disconnect(true);
  WiFi.begin(AP_SSID, AP_PASSWORD);

  while(WiFi.status() != WL_CONNECTED) {
    delay(1000);
    
    Serial.print(".");
  }

  Serial.println("Connected to the access point !");

  CONNECTED_TO_WIFI = true;

  digitalWrite(WIFI_AP_LED_CONTROLLER, HIGH);
}

// ------------------------------------------------------------------------------------------------------------

void WifiClientHandling() {
  Serial.println("Connecting to the websocket server... please wait...");

  discoveryWebSocketClient.begin(WEBSOCKET_SERVER_HOST, WEBSOCKET_SERVER_PORT, "/discovery", "ws");
  discoveryWebSocketClient.onEvent(onDiscoveryEvent);

  temperatureWebSocketClient.begin(WEBSOCKET_SERVER_HOST, WEBSOCKET_SERVER_PORT, "/temperature", "ws");
  temperatureWebSocketClient.onEvent(onTemperatureEvent);
}

// ------------------------------------------------------------------------------------------------------------

void SendDiscoveryMessage() {
    Serial.println("Identifying current device to the websocket server... please wait...");

    StaticJsonDocument<100> device;
    device["serialNumber"] = DEVICE_SERIAL_NUMBER;
    device["room"] = ROOM_NAME;

    String deviceAsJson;
    serializeJson(device, deviceAsJson);

    discoveryWebSocketClient.sendTXT(deviceAsJson);
}

void SendTemperatureValue() {
  long t = millis();
  long dt = t - LAST_TEMPERATURE_SNAPSHOT;

  if(dt > TEMPERATURE_SNAPSHOT_PERIOD) {
    StaticJsonDocument<100> temperature;
    temperature["serialNumber"] = DEVICE_SERIAL_NUMBER;
    temperature["value"] = random(0, 55);

    String temperatureAsString;
    serializeJson(temperature, temperatureAsString);

    temperatureWebSocketClient.sendTXT(temperatureAsString);
  
    LAST_TEMPERATURE_SNAPSHOT = t;
  }
}

// ------------------------------------------------------------------------------------------------------------
// ------------------------------------------------------------------------------------------------------------

void onDiscoveryEvent(WStype_t type, uint8_t * payload, size_t length) {
    switch (type) {
        case WStype_DISCONNECTED: {
            Serial.println("Disconnected from discovery websocket");
            
            if(CONNECTED_TO_DISCOVERY_WEBSOCKET) {
              digitalWrite(WEBSOCKET_DISCOVERY_LED_CONTROLLER, LOW);
              
              CONNECTED_TO_DISCOVERY_WEBSOCKET = false;
              IDENTIFIED_TO_DISCOVERY_WEBSOCKET = false;
            }

            break;
        }
        case WStype_CONNECTED: {
            Serial.println("Connected to discovery websocket");

            digitalWrite(WEBSOCKET_DISCOVERY_LED_CONTROLLER, HIGH);
            CONNECTED_TO_DISCOVERY_WEBSOCKET = true;

            break;
        }
        case WStype_TEXT: {               
            Serial.println("Identified to discovery websocket");

            IDENTIFIED_TO_DISCOVERY_WEBSOCKET = true;

            break;
        }
    }
}

void onTemperatureEvent(WStype_t type, uint8_t * payload, size_t length) {
    switch (type) {
        case WStype_DISCONNECTED: {
            Serial.println("Disconnected from temperature websocket");
            
            if(CONNECTED_TO_TEMPERATURE_WEBSOCKET) {
              digitalWrite(WEBSOCKET_TEMPERATURE_LED_CONTROLLER, LOW);
              
              CONNECTED_TO_TEMPERATURE_WEBSOCKET = false;
            }

            break;
        }
        case WStype_CONNECTED: {
            Serial.println("Connected to temperature websocket");

            digitalWrite(WEBSOCKET_TEMPERATURE_LED_CONTROLLER, HIGH);
            CONNECTED_TO_TEMPERATURE_WEBSOCKET = true;

            break;
        }
        case WStype_TEXT: {   
            String response = (char*) payload;

            Serial.printf("Temperature state response: %s\n", payload);
            
            if(response == "409")
              buzzer();

            break;
        }
    }
}

void buzzer() {
  Serial.println("Going to buzz a little bit !");
}

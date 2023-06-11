#include "RestClient.h"
#include <PubSubClient.h>
#include "ArduinoJson.h"
#include <ESP8266WiFi.h>
#include <Adafruit_Sensor.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <ArduinoHttpClient.h>


int test_delay = 4000; //so we don't spam the API
boolean describe_tests = true;

//double concentracion = 0.0;
RestClient client = RestClient("192.168.43.223", 8080);//ip modificar

//variables para conexión ESP8266/wifi
IPAddress serverAddress(192, 168, 43, 223);
WiFiClient wifi_http; int port_http = 8080;
HttpClient client_http = HttpClient(wifi_http, serverAddress, port_http);

#define STASSID "Redmi" //modificar no 5g
#define STAPSK  "81a34c32d184" // modificar

//DUDAAA///////////////////////////////////////////////////////////////////////////////////////////////////
//Adafruit_Sensor sensor(D1,Adafruit_Sensor); //esto por qué lo pones?
//Define NTP Client to get time
const long utcOffsetInSeconds = 7200;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP,"es.pool.ntp.org", utcOffsetInSeconds);

double concentracion;



String response;

String serializeBody(double concentracion, long fecha, int idPlaca)
{
  StaticJsonDocument<200> doc;


  // Add values in the document
  //
    //doc["idConcentracion"] = idConcentracion;    
    doc["concentracion"] = concentracion;    
    doc["fecha"] = fecha;
    doc["idPlaca"] = idPlaca;        


  // Add an array.

  // Generate the minified JSON and send it to the Serial port.
  //
  String output;
  serializeJson(doc, output);
  // The above line prints:

  // Start a new line
  Serial.println(output);

  return output;
}

void test_status(int statusCode)
{
  delay(test_delay);
  if (statusCode == 200 || statusCode == 201)
  {
    Serial.print("TEST RESULT: ok (");
    Serial.print(statusCode);
    Serial.println(")");
  }
  else
  {
    Serial.print("TEST RESULT: fail (");
    Serial.print(statusCode);
    Serial.println(")");
  }
}

void deserializeBody(String responseJson){
  if (responseJson != "")
  {
    StaticJsonDocument<200> doc;


    // Deserialize the JSON document
    DeserializationError error = deserializeJson(doc, responseJson);

    // Test if parsing succeeds.
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // Fetch values.
    //
    // Most of the time, you can rely on the implicit casts.
    // In other case, you can do doc["time"].as<long>();
    //const char *idConcentracion = doc["idConcentracion"];
  
    double concentracion = doc["concentracion"];
    long fecha = doc["fecha"];
    int idPlaca = doc["idPlaca"];

    // Print values.
    //Serial.println(idConcentracion);
    Serial.println(concentracion);
    Serial.println(fecha);
    Serial.println(idPlaca);
  }
}

void test_response()
{
  Serial.println("TEST RESULT: (response body = " + response + ")");
  response = "";
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}


void POST_tests()
{
  timeClient.update();
  //concentracion = analogRead(A0);//modifcado
  //concentracion = random(1.0, 20.0);

  String post_body = serializeBody(analogRead(A0),millis(), 3);

  describe("Test POST with path and body and response");
  test_status(client.post("/api/concentracion", post_body.c_str(), &response));
  
 // String contentType = "application/json";
  //client_http.beginRequest();
  //client_http.post("/api/concentracion",contentType, post_body.c_str());
  test_response();
  //client_http.endRequest();
  delay(5000);
}

//Setup
void setup()
{
  Serial.begin(9600);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  /* Explicitly set the ESP8266 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);
  

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");

  //sensor.begin(); //error de la línea 20
  timeClient.begin();

  
}

// Run the tests!
void loop()
{
  POST_tests();
}
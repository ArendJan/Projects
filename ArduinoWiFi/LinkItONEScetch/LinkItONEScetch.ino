/*
 * This code is evolved from the example wifiserver(both the LinkIt ONE example and the Arduino example, created by Arend-Jan van Hilten
 */
#include <LTask.h>
#include <LWiFi.h>
#include <LWiFiServer.h>
#include <LWiFiClient.h>
#define WIFI_AP "van_Hilten"
#define WIFI_PASSWORD "Pinksteren1"//Fill in your own things
#define WIFI_AUTH LWIFI_WPA  // choose from LWIFI_OPEN, LWIFI_WPA, or LWIFI_WEP according to your WiFi AP configuration

LWiFiServer server(80);
void setup()
{
  pinMode(A1, INPUT);
  //Start the wifi connection and the server.
  LWiFi.begin();
  // keep retrying until connected to AP

  while (0 == LWiFi.connect(WIFI_AP, LWiFiLoginInfo(WIFI_AUTH, WIFI_PASSWORD)))
  {
    delay(10);
  }
  server.begin();

}


void loop()
{
  LWiFiClient client = server.available();
  if (client)
  {
    int countWhile = 0;
    boolean cancel = true;
    String currentLine = "";                // make a String to hold incoming data from the client
    String response="asdf;jkl;j;jl;jl;jl;kj;oiho;hu;hu;jhl;jhlkjl;jl;kjl;kjl;jl;kjl;kj;lkjl;kjl;kj;lkjlk;jkl";
    while (client.connected() && cancel) {          // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        int i = client.read();    //Some other voodoo to correctly retrieve the data
        char c = char(i);
        
        if (c == '\n') {                    // if the byte is a newline character

          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println();
            client.print(response);     //This is set earlier, and needs to be send here, because else newer phones don't like it, android 2.3 doesn't care, but 6.0 does. Then the response is sent earlier than the Http header.
            client.println();


            // break out of the while loop:
            break;
          }
          else {      // if you got a newline, then clear currentLine:
            currentLine = "";
          }
        }
        else if (c != '\r') {    // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }
        if (currentLine.endsWith("GET /START")) {    //If the next button is pressed
          response = "YES";
          
          
        }
        else if(currentLine.endsWith("GET /GET")){
          response = String(analogRead(A1));
          
          
        }
       //You can add every get statement eg: "GET /"
       
      }
      if (countWhile > 2000) {  //The wifi library is sometimes really buggy, especially when requesting data using a browser, android is not a problem
      cancel = false;           //So this is to prevent staying in the loop infinitly
      }
      delayMicroseconds(10);    //The 2000 looping is done really fast.
    }
    client.stop();
  }
}


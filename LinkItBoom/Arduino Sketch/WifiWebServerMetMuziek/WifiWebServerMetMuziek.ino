/*
 * This code is evolved from the example wifiserver(both the LinkIt ONE example and the Arduino example, created by Arend-Jan
 */
#include <LTask.h>
#include <LWiFi.h>
#include <LWiFiServer.h>
#include <LWiFiClient.h>

#include <LStorage.h>

#include <LSD.h>
//#include<LFlash.h>
#include <LAudio.h>

#define WIFI_AP "van_Hilten"
#define WIFI_PASSWORD "Pinksteren1"
#define WIFI_AUTH LWIFI_WPA  // choose from LWIFI_OPEN, LWIFI_WPA, or LWIFI_WEP according to your WiFi AP configuration

LWiFiServer server(80);
String muziekjes[50] = {};
int CurrentSong = 0;
int SongsAmount = 0;
int volume = 3;
LFile myFile;
int previousState;
int counter;
boolean pauseBool = false;
void setup()
{
  previousState = 10;//this is the variable used for storing the current audiostatus, because getStatus contains a bug....
  //Serial.begin(9600);
  //Serial.println("Starting");
  pinMode(13, OUTPUT);
  LAudio.begin();
  LAudio.setVolume(volume);
  if (!LSD.begin()) {
    return;
  }

  myFile = LSD.open("test.txt"); //the songsFile
  if (myFile) {
    String fileread = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf";// just to be sure;
    counter = 0;
    fileread = "";
    // read from the file until there's nothing else in it:
    while (myFile.available()) {
      int i = myFile.read();  //Some voodoo to make sure the character is added to the fileread var, without this, the ascii number is added(C logic)
      char c = char(i);
      fileread = fileread + c;
      if (fileread.endsWith("mp3 ")) {//Found a song
        fileread.remove(fileread.length() - 1);//Remove the <space>
        muziekjes[counter] = fileread;
        fileread = "";
        counter++;

      }
    }
    SongsAmount = counter;
    // close the file:
    myFile.close();
  } else {
    // the file didn't open
  }
  SongsAmount = counter;
  counter = -1;

  nextSong();//Start the first song
  //Serial.println("Starting wifi");
  LWiFi.begin();
  //Serial.begin(115200);

  // keep retrying until connected to AP
  //Serial.println("Connecting to AP");
  while (0 == LWiFi.connect(WIFI_AP, LWiFiLoginInfo(WIFI_AUTH, WIFI_PASSWORD)))
  {
    delay(10);
    //Serial.println("Connecting");
  }

  // printWifiStatus();

  //Serial.println("Start Server");
  server.begin();
  //Serial.println("Server Started");

}

int loopCount = 0;

void loop()
{
  //LCD.CleanAll(WHITE);    //Clean the screen with black or white.
  // put your main code here, to run repeatedly:
  int state = getstate();
  //Serial.println("state="+state);
  //  LCD.FontModeConf(Font_6x8, FM_ANL_AAA, BLACK_BAC);
  //    LCD.DispStringAt("Hello World!", 0, 10);
  if (state == AudioEndOfFile || state == AudioCommonFailed) {//If a song is done or there is an error, start the next song;
    nextSong();
    //Serial.println("Started nextSong()");
  }

  delay(100);
  loopCount++;
  LWiFiClient client = server.available();
  if (client)
  {
    int countWhile = 0;
    boolean cancel = true;
    String currentLine = "";                // make a String to hold incoming data from the client
    String response="asdf";
    while (client.connected() && cancel) {          // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        int i = client.read();
        char c = char(i);

        //Serial.write(c);                    // print it out the serial monitor
        if (c == '\n') {                    // if the byte is a newline character

          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println();
            client.print(response);
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
        //Serial.println("Currentline:"+currentLine);
        // Check to see if the client request was "GET /H" or "GET /L":
        if (currentLine.endsWith("GET /NEXT")) {
          //client.println("");
          //Serial.println("yayayayayayayayayayayayayayayayyayaH");
          nextSong();
          response = muziekjes[counter];
          
          
        }
        if (currentLine.endsWith("GET /PLAY")) {
          //client.println("");
          //Serial.println("yayayayayayayayayayayayayayayayyayaL");
          pauseResume(); 
          response = muziekjes[counter];
          //client.print(muziekjes[counter]);
          
          //digitalWrite(13, LOW);                // GET /L turns the LED off
        }
        if (currentLine.endsWith("GET /SONG")) {
          //client.println("");
          response = muziekjes[counter];
          //client.print(muziekjes[counter]);
          //client.println("");
        }
        if (currentLine.endsWith("GET /UP")) {
          //client.println("");
          if(volume!=7){
            setV(volume+1);
            volume++;
            
          }
          response = String(volume);
         
        }
        if (currentLine.endsWith("GET /DOWN")) {
          //client.println("");
          if(volume!=0){
            setV(volume-1);
            volume--;
            
          }
          response = String(volume);
          //client.print(volume);
          
        }
        if (currentLine.endsWith("GET /VOL")) {
          
          response = String(volume);
         
        }
       
      }
      if (countWhile > 2000) {
      cancel = false;
      }
      delayMicroseconds(10);
    }
    // close the connection:
    //client.stop();


    // give the web browser time to receive the data
   // delay(500);

    // close the connection:
    //Serial.println("close connection");
    client.stop();
    //Serial.println("client disconnected");
  }
}


void nextSong() {
  pauseBool = false;
  bool klaar = false;
  while (!klaar) {
    counter++;
    if (counter == SongsAmount) {
      counter = 0;
    }
    char* buf = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf"; //just to be sure;
    muziekjes[counter].toCharArray(buf, muziekjes[counter].length() + 1); //or else it becomes musicFile.mp;
    
    if (!LSD.exists((char*)buf)) {
      klaar = false;
    }
    else
    {
      klaar = true;
      LAudio.playFile(storageSD, (char*)buf);
    }
  }
  previousState = 10;//Used a random number for stating that the audio is playing(getstatus bug)
}
void pauseResume() {
  //Serial.println("pauseResume");
  int Astatus = getstate();
  if (pauseBool) { //Couln't find the enum for the status when it is playing.
    //Serial.println("resume();");
    LAudio.resume();
    pauseBool = false;
    previousState = 10;
  }
  else if (pauseBool == false) {
    pauseBool = true;
    //Serial.println("pause();");
    LAudio.pause();
    previousState = AudioPause;
  }

}
int getstate() {

  AudioStatus state = LAudio.getStatus();
  if (state ==  0) {
    return previousState;
  }
  else {
    previousState = state;
    return state;
  }

}
void setV(int v){
  LAudio.setVolume(v);
}


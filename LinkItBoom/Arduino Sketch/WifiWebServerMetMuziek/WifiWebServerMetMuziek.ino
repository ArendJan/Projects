/*
 * This code is evolved from the example wifiserver(both the LinkIt ONE example and the Arduino example, created by Arend-Jan van Hilten
 */
#include <LTask.h>
#include <LWiFi.h>
#include <LWiFiServer.h>
#include <LWiFiClient.h>

#include <LStorage.h>

#include <LSD.h>
#include <LAudio.h>

#define WIFI_AP "van_Hilten"
#define WIFI_PASSWORD "Pinksteren1"//Fill in your own things
#define WIFI_AUTH LWIFI_WPA  // choose from LWIFI_OPEN, LWIFI_WPA, or LWIFI_WEP according to your WiFi AP configuration

LWiFiServer server(80);
String muziekjes[50] = {};  //The string array to store all the music paths.
int CurrentSong = 0;        //The current song that is being played, used for nextsong and sending the title back to the phone.
int SongsAmount = 0;        //The total amount of songs, calculated in the setup()
int volume = 3;             //The initial volume, you can change it.
LFile myFile;
int previousState;         //Used for the current state, playing, pause...
int counter;              //for in the setup, and used also in nextsong
boolean pauseBool = false;//If the music is paused
void setup()
{
  previousState = 10;//this is the variable used for storing the current audiostatus, because getStatus contains a bug....
  pinMode(13, OUTPUT);    //IDK why this doesn't work, but the led doesn't change.
  LAudio.begin();         //Start the audio
  LAudio.setVolume(volume);
  if (!LSD.begin()) {     //If the LinkIt can't get high
    return;
  }

  myFile = LSD.open("test.txt"); //the file with all the songs
  if (myFile) {
    String fileread = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf";// just to be sure;
    counter = 0;
    fileread = "";
    // read from the file until there's nothing else in it:
    while (myFile.available()) {
      int i = myFile.read();  //Some voodoo to make sure the character is added to the fileread var, without this, the ascii number is added(C logic)
      char c = char(i);
      fileread = fileread + c;
      if (fileread.endsWith("mp3 ")) {//Found a song ( the text file is: "asdf.mp3<space>asdf2.mp3<space>" )
        fileread.remove(fileread.length() - 1);//Remove the <space>
        muziekjes[counter] = fileread;        //Add the file to the array.
        fileread = "";
        counter++;

      }
    }
    SongsAmount = counter;
    // close the file:
    myFile.close();
  } else {
    while(1);// the file didn't open, then don't do a thing
  }
  SongsAmount = counter;
  counter = -1;     //because the counter is increased in nextSong, so it starts at place 0.

  nextSong();//Start the first song

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
  int state = getstate();
  if (state == AudioEndOfFile || state == AudioCommonFailed) {//If a song is done or there is an error, start the next song;
    nextSong();
  }


  delay(100);

  
  LWiFiClient client = server.available();
  if (client)
  {
    int countWhile = 0;
    boolean cancel = true;
    String currentLine = "";                // make a String to hold incoming data from the client
    String response="asdf";
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
        if (currentLine.endsWith("GET /NEXT")) {    //If the next button is pressed
          nextSong();
          response = muziekjes[counter];
          
          
        }
        if (currentLine.endsWith("GET /PLAY")) {    //Play/pause button
          pauseResume(); 
          response = muziekjes[counter];
        }
        
        if (currentLine.endsWith("GET /SONG")) {    //Reload button for the song part.
          response = muziekjes[counter];
        }
        
        if (currentLine.endsWith("GET /UP")) {    //The volume up
          if(volume!=7){
            setV(volume+1);
            volume++;
          }
          response = String(volume);
        }
        
        if (currentLine.endsWith("GET /DOWN")) {    //Volume down
          if(volume!=0){
            setV(volume-1);
            volume--;
          }
          response = String(volume);
        }
        
        if (currentLine.endsWith("GET /VOL")) {   //reload button volume part
          response = String(volume);
        }
       
      }
      if (countWhile > 2000) {  //The wifi library is sometimes really buggy, especially when requesting data using a browser, android is not a problem
      cancel = false;           //So this is to prevent staying in the loop infinitly
      }
      delayMicroseconds(10);    //The 2000 looping is done really fast.
    }
    client.stop();
  }
}


void nextSong() {   //Start the next song.
  pauseBool = false;      //it is not paused
  bool klaar = false;
  while (!klaar) {
    counter++;    //increase the counter.
    if (counter == SongsAmount) {
      counter = 0;      //When it is at the end
    }
    char* buf;
    muziekjes[counter].toCharArray(buf, muziekjes[counter].length() + 1); //or else it becomes musicFile.mp;
    
    if (!LSD.exists((char*)buf)) {    //Just to be sure it exists.
      klaar = false;    //If not existing, redo the loop, so go for the next song.
    }
    else
    {
      klaar = true;     //yay, the song exists, so start it and end the loop
      LAudio.playFile(storageSD, (char*)buf);
    }
  }
  previousState = 10;//Used a random number for stating that the audio is playing(getstatus bug)
}
void pauseResume() {
  if (pauseBool) { //If paused, resume.
    LAudio.resume();
    pauseBool = false;
    previousState = 10;
  }
  else if (pauseBool == false) {  //if not paused, pause
    pauseBool = true;
    LAudio.pause();
    previousState = AudioPause;
  }

}
int getstate() {//This is used because of a strange behaviour, but now it is not really necessary. 

  AudioStatus state = LAudio.getStatus();
  if (state ==  0) {
    return previousState;
  }
  else {
    previousState = state;
    return state;
  }

}
void setV(int v){   //Set the volume, IDK why in a extra function.
  LAudio.setVolume(v);
}


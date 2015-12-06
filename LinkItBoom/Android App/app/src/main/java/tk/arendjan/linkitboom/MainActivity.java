package tk.arendjan.linkitboom;
/*
This is the LinkitBoom App for the Hackster.IO community, made by Arend-Jan van Hilten(http://arendjan.tk). This app has to be used with the LinkIt ONE
running the LinkitBoom arduino code, found somewhere. You can modify everything, but it would be nice if you state me somewhere and
send me a message about it, so I can check it out :P.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends AppCompatActivity {

    public String ip;       //This is the IP address of the LinkIt ONE.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//Bla bla bla, I don't know this.

        //This gets the saved IP address, and puts it in the variable, if not set, then it starts the Setting activity.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ip = preferences.getString("IP", "NONE");
        if(ip.equals("NONE")){
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
        }
        else{
            requestAll();
        }



        //This is of the Reload button, it gets the saved IP address and stores it and starts a request to show the current song.
        final Button reload = (Button) findViewById(R.id.reloadB);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                ip = preferences.getString("IP", "");
                requestAll();

            }
        });
        //The pause button: just fires a http://IP/PLAY request, with a "1", to say the outcome must be put in the CurrentSongLabel
        final Button pause = (Button) findViewById(R.id.pauseplayB);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RequestTask().execute(ip+"PLAY", "1");

            }
        });

        //Next button: starts a http://IP/NEXT request, with a "1" for the correct label.
        final Button nextSong = (Button) findViewById(R.id.playnextB);
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RequestTask().execute(ip+"NEXT", "1");

                }

        });

        //VolumeBotton: fires a request for the ip/UP 'page', with a "2" for the VolumeLabel
        Button volumeUp = (Button) findViewById(R.id.upVB);
        volumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RequestTask().execute(ip+"UP", "2");

            }
        });

        //VolumeBottonDown: fires a request for the ip/DOWN 'page', with a "2" for the VolumeLabel
        Button volumeDown = (Button) findViewById(R.id.downVB);
        volumeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RequestTask().execute(ip+"DOWN", "2");

            }
        });
    }
    private void requestAll(){
        new RequestTask().execute(ip+"SONG", "1");
        new RequestTask().execute(ip+"VOL", "2");
    }
    //Some standard blablabla for the menu etc.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //The actual task which requests the data from the LinkIt ONE.
    //Can be fired with: new RequestTask().execute(String url, String MODE(1 for currentSongLabel, 2 for VolumeLabel));
    class RequestTask extends AsyncTask<String, String, String> {
        private int MODE = 0; //1 == title of song , 2 == volume
        private boolean error = false;
        @Override
        protected String doInBackground(String... uri) {

            MODE = Integer.parseInt(uri[1]);    //Set the mode.

            String responseString;

            //Try to get the data from the LinkIt ONE.
            try {
                URLConnection connection = new URL(uri[0]).openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);

                }
                responseString = sb.toString();

                //If something goes wrong.
            } catch (Exception e){
                error = true;
                responseString = e.getLocalizedMessage();
            }

            return responseString;
        }

        //After requesting the data.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //remove the extra texts, .mp3 because the whole filename is returned. HTTP/blablabla because the LinkIt ONE WIFI library is shit.
            result = result.replace(".mp3","").replace("HTTP/1.1 200 OKContent-type:text/html", "");
            if(!error){
                //Everything OK
                if(MODE == 0){
                    //Fuck, something is not ok, because the MODE is something that can't be.
                    Toast.makeText(MainActivity.this, "Starting the Async went wrong", Toast.LENGTH_LONG).show();
                }
                else if(MODE == 1){
                    //Put the result in the currentSongLabel.
                    TextView textView = (TextView) findViewById(R.id.currentsongL);
                    textView.setText(result);
                }
                else if(MODE == 2){
                    //Put the result in the VolumeLabel
                    TextView textView = (TextView) findViewById(R.id.volumeL);
                    textView.setText(result);
                }

            }
            else{
                //A catch method caught an error.
                Toast.makeText(MainActivity.this, "Oops, something went wrong.",Toast.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "Oops, something went wrong. The error is:" + result, Toast.LENGTH_LONG).show();//Use this to see the error.
            }

        }
    }


}

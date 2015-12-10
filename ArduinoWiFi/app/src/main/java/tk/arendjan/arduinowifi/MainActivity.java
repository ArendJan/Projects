package tk.arendjan.arduinowifi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //This gets the saved IP address, and puts it in the variable, if not set, then it starts the Setting activity.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ip = preferences.getString("IP", "NONE");
        if(ip.equals("NONE")){
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
        }
        else{

            //requestAll();
            //This is when the app is started, you can already retrieve data.
        }

        final Button reload = (Button) findViewById(R.id.reloadB);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When coming from the settings page, you need to press teh reload button.
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                ip = preferences.getString("IP", "");
                //requestAll();

            }
        });
        final Button get = (Button) findViewById(R.id.getB);
        get.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new RequestTask().execute(ip + "GET", "1"); //Change the "GET" string to a string that is checked by the Arduino.
                //The "1" is for the correct textview, if you have more than 1,
                // you can set the correct destination of the output, or what has to be done with it.
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //some blabla for creating the menu
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override //Some blablabla for a pressed menu item.
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {//This starts the Setting class.
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //The actual task which requests the data from the Arduino.
    //Can be fired with: new RequestTask().execute(String url, String MODE(1 for main label, rest you can change/add));
    class RequestTask extends AsyncTask<String, String, String> {
        private int MODE = 0; //1 == title of song , 2 == volume
        private boolean error = false;
        @Override
        protected String doInBackground(String... uri) {

            MODE = Integer.parseInt(uri[1]);    //Set the mode.

            String responseString;

            //Try to get the data from the LinkIt ONE. Do not change this unless you know what this code does!
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

            //remove the extra texts, HTTP/blablabla because the LinkIt ONE WIFI library is shit.
            result = result.replace("HTTP/1.1 200 OKContent-type:text/html", "");
            if(!error){
                //Everything OK
                if(MODE == 0){
                    //Fuck, something is not ok, because the MODE is something that can't be.
                    Toast.makeText(MainActivity.this, "Starting the Async went wrong", Toast.LENGTH_LONG).show();
                }
                else if(MODE == 1){
                    TextView textView = (TextView) findViewById(R.id.dataV);
                    textView.setText(result);
                }
                else if(MODE == 2){

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

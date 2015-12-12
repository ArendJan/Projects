package tk.arendjan.arduinowifi;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Setting extends AppCompatActivity {
    public ProgressDialog dialog; //The dialog, used when trying to get the data from a IP.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final TextView input = (TextView) findViewById(R.id.ipTF);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String IP = preferences.getString("IP", "");
        input.setText(IP.replace("http://", "").replace("/", "")); //Fill in the previous set IP in the label.

        //When the ok button is clicked, show the dialog and fire of a request.
        Button ok = (Button) findViewById(R.id.okB);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = ProgressDialog.show(Setting.this, "Checking", "Checking the IP.", false);
                String IP = input.getText().toString();
                IP = "http://" + IP + "/";
                new RequestTask().execute(IP + "START", IP);//Checking if the thing is up

            }
        });
    }


    //The request class.
    class RequestTask extends AsyncTask<String, String, String> {
        private String IP = "";//the variable to store the IP address to store in the preferences.
        private boolean error = false;
        @Override
        protected String doInBackground(String... uri) {

            IP = uri[1];    //Store the IP
            String responseString;

            try {
                URLConnection connection = new URL(uri[0]).openConnection();
                connection.setRequestProperty("Connection", "close");
                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);

                }
                responseString = sb.toString();
            }
            //Oops, something  went wrong.
            catch (Exception e){
                error = true;
                responseString = e.getLocalizedMessage();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {

            //Jup, first close the dialog, because it is a bit safer/shorter and the time this takes is not that much.
            dialog.dismiss();

            Toast.makeText(Setting.this, result, Toast.LENGTH_LONG).show();
            //If there isn't an exception.
            if(!error){
                if(result.equals("YES")) {


                    //Save the IP address.
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Setting.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("IP", IP);
                    editor.apply();
                    //FINISH HIM, and go back to the mainactivity.
                    Setting.this.finish();

                }
                else{
                    Toast.makeText(Setting.this, "This is a server, but it is not the Arduino/LinkIt ONE running the correct software.", Toast.LENGTH_LONG).show();
                }
            }
            else{

                //If there is an exception, say something.
                Toast.makeText(Setting.this, "Oops, something went wrong. Error message:" + result,Toast.LENGTH_LONG).show();

            }

        }
    }
}

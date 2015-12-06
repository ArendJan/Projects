package tk.arendjan.linkitboom;

import android.app.ProgressDialog;
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




public class Setting extends AppCompatActivity {
    public ProgressDialog dialog; //The dialog, used when trying to get the data from a IP.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Get the IP that is currently set and put it in the textfield.
        final TextView input = (TextView) findViewById(R.id.IPT);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String IP = preferences.getString("IP", "");
        input.setText(IP.replace("http://", "").replace("/",""));

        //When the ok button is clicked, show the dialog and fire of a request.
        Button ok = (Button) findViewById(R.id.OKB);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 dialog = ProgressDialog.show(Setting.this, "Checking", "Checking the IP.", false);
                 String IP = input.getText().toString();
                IP = "http://" + IP + "/";
                new RequestTask().execute(IP+"SONG", IP);

            }
        });
    }

    //Some blablabla for the menu which isn't there.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_setting, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                //Save the IP address.
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Setting.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("IP", IP);
                editor.apply();
                //FINISH HIM, and go back to the mainactivity.
                Setting.this.finish();


            }
            else{

                //If there is an exception, say something.
                Toast.makeText(Setting.this, "Oops, something went wrong. Error message:" + result,Toast.LENGTH_LONG).show();

            }

        }
    }
}


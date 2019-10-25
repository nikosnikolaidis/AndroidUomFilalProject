package com.example.stergios.androiduomproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class SplashActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private Toast toast;
    static boolean isActivityActive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //arxikopiithite edw kai stin sinexia xrisimopihte
        //to idio gia opiodipote minima oste na apofigoume
        //tin dimiourgia pollwn toast stin sira
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        if(prefs.getString(getString(R.string.pref_location_code_key), getString(R.string.pref_location_code_default)).equals(getString(R.string.pref_location_code_default))){
            inputLocationDialog();
        }
        else{
            //perimenoume ligo gia na min einai apotomi i enalagi
            //gia kaliteri optiki empiria
            try{
                Thread.sleep(1000);
            }catch (Exception e){}
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        isActivityActive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityActive = false;
    }


    void inputLocationDialog() {

        final View v = View.inflate(SplashActivity.this, R.layout.location_input_alert_dialog_content, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder .setView(v)
                .setCancelable(false)
                .setPositiveButton("Save", null);
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        final EditText locationInput = (EditText) v.findViewById(R.id.location_input);
                        if(locationInput.getText().length()<3){
                            toast.setText("Enter at least 3 characters!");
                            toast.show();
                        }
                        else{
                            alert.dismiss();
                            locationSearch(locationInput.getText().toString());
                        }


                    }
                });
            }
        });

        alert.show();
    }

    void locationSearch(final String location){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pDialog = new ProgressDialog(SplashActivity.this);
                        pDialog.setMessage("Searching...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                    }
                });



                HttpURLConnection urlConnection=null;
                try{
                    String urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey="+MainActivity.amadeusKey+
                            "&term="+location;
                    URL url = new URL(urlAddress);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(10000);

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();
                    String response = "";
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        response += current;
                    }

                    JSONArray jsonRootObject = new JSONArray(response);

                    final JSONObject jsonObject = jsonRootObject.getJSONObject(0);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(getResources().getString(R.string.pref_location_code_key), jsonObject.optString("value"));
                    editor.putString(getResources().getString(R.string.pref_location_key), jsonObject.optString("label"));
                    editor.commit();
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isActivityActive){
                                toast.setText(jsonObject.optString("label")+" has been set as primary location.");
                                toast.show();
                                pDialog.dismiss();
                                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                SplashActivity.this.startActivity(i);
                                SplashActivity.this.finish();
                            }
                        }
                    });

                }catch (UnknownHostException e){
                    //sinithos afto to error emfanizete otan den iparxi sindesi
                    SplashActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if(isActivityActive){
                                toast.setText("Check your internet connection");
                                toast.show();
                                pDialog.dismiss();
                                SplashActivity.this.inputLocationDialog();
                            }

                        }
                    });
                }catch(Exception e){

                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isActivityActive){
                                toast.setText("No such city with airport, try again please.");
                                toast.show();
                                pDialog.dismiss();
                                SplashActivity.this.inputLocationDialog();
                            }
                        }
                    });
                    e.printStackTrace();
                }
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
            }
        }).start();
    }



}
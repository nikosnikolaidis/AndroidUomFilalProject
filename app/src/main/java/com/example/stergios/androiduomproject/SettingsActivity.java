package com.example.stergios.androiduomproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
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

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    static boolean isActivityActive=false;
    private AppCompatDelegate mDelegate;
    private Toast toast;
    private ProgressDialog pDialog;
    static boolean checkLocationChange= true; //gia elenxo egkirotitas tis topothesias mono otan allazei apo ton xristi
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        //arxikopiithite edw kai stin sinexia xrisimopihte
        //to idio gia opiodipote minima oste na apofigoume
        //tin dimiourgia pollwn toast stin sira
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);


        Preference locationPref = findPreference(getResources().getString(R.string.pref_location_key));
        locationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                inputLocationDialog(preference);
                return  true;
            }
        });

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_cur_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String stringValue = value.toString();


        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        isActivityActive = true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
        isActivityActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }


    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return true;

        }


    }

    void inputLocationDialog(final Preference preference) {

        final View v = View.inflate(SettingsActivity.this, R.layout.location_input_alert_dialog_content, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder .setView(v)
                .setCancelable(true)
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
                            locationSearch(locationInput.getText().toString(), preference);
                        }


                    }
                });
            }
        });

        alert.show();
    }

    void locationSearch(final String location,final Preference preference){
        new Thread(new Runnable() {
            @Override
            public void run() {

                SettingsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pDialog = new ProgressDialog(SettingsActivity.this);
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
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(getResources().getString(R.string.pref_location_code_key), jsonObject.optString("value"));
                    editor.putString(getResources().getString(R.string.pref_location_key), jsonObject.optString("label"));
                    editor.commit();
                    SettingsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isActivityActive){
                                pDialog.dismiss();
                                preference.setSummary(jsonObject.optString("label"));
                            }
                        }
                    });



                }catch (UnknownHostException e){
                    //sinithos afto to error emfanizete otan den iparxi sindesi
                    SettingsActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            toast.setText("Check your internet connection");
                            toast.show();
                            pDialog.dismiss();
                        }
                    });
                }catch(Exception e){

                    SettingsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isActivityActive){
                                toast.setText("No such city with airport, try again please.");
                                toast.show();
                                pDialog.dismiss();
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
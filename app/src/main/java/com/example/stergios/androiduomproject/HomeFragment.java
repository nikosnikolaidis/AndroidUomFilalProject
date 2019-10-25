package com.example.stergios.androiduomproject;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class HomeFragment extends android.support.v4.app.Fragment {
    private ImageView logo;
    private String originCity="";
    private String randomDestination="";
    private Toast toast;
    private AnimationDrawable planeAnim;
    static Thread newLuckyTrip= new Thread();
    static boolean stopThread=false;


    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        //arxikopoieitai edw kai stin sinexia xrisimopihte
        //to idio gia opiodipote minima oste na apofigoume
        //tin dimiourgia pollwn toast stin sira
        toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);

        logo = (ImageView) rootView.findViewById(R.id.Logo);
        planeAnim=new AnimationDrawable();
        for(int i=1; i<=16;i++){
            try {
                InputStream ims = getActivity().getAssets().open("plane_frames/plane"+i+".png");
                Drawable d = Drawable.createFromStream(ims, null);
                planeAnim.addFrame(d, 20);
            }
            catch(Exception e){e.printStackTrace();}
        }
        planeAnim.setOneShot(false);
        logo.setBackgroundDrawable(planeAnim);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast.setText("Sometimes luck is late, be patient!");
                toast.show();
                if(!newLuckyTrip.isAlive()){
                    stopThread=false;

                    newLuckyTrip= new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(getActivity()!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        planeAnim.start();
                                    }
                                });
                            }

                            HttpURLConnection urlConnection=null;
                            boolean retry = false;
                            try{

                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                originCity = prefs.getString(getString(R.string.pref_location_code_key), getString(R.string.pref_location_code_default));


                                //pernoume random geografikes sintetagmenes
                                //kai stin sinexia pernoume to kontinotero aerodromio giaftes
                                Random r = new Random();
                                double randomLatitude = 1 + (100 - 1) * r.nextDouble() - 40;
                                double randomLongitude= 1 + (230 - 1)* r.nextDouble() -100;

                                String urlAddress="http://api.sandbox.amadeus.com/v1.2/airports/nearest-relevant?latitude="+randomLatitude+"&longitude="+randomLongitude+"&apikey="+MainActivity.amadeusKey;
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

                                JSONObject jsonObject = jsonRootObject.getJSONObject(0);

                                randomDestination=jsonObject.optString("airport");

                                //pernei random imerominia stis epomenes 20 meres
                                r = new Random();
                                int  randomDay = r.nextInt(20) + 1;
                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat df = new SimpleDateFormat("y-MM-dd");
                                c.add(Calendar.DATE, randomDay);
                                String departureDate= df.format(c.getTime());
                                //2 meres evros anazitisis ptisis
                                c.add(Calendar.DATE, 2);
                                departureDate+="--"+df.format(c.getTime());
                                //6 meres meta ws i maximum imerominia epistrofis
                                c.add(Calendar.DATE, 6);
                                String returnDate= df.format(c.getTime());

                                //pernei to prokathorismeno nomisma
                                prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                String cur = prefs.getString(getString(R.string.pref_cur_key), getResources().getString(R.string.pref_cur_eur));

                                //anazitei ptisi
                                urlAddress="http://api.sandbox.amadeus.com/v1.2/flights/low-fare-search?apikey="+
                                        MainActivity.amadeusKey+"&origin="+originCity+"&destination="+
                                        randomDestination+"&departure_date="+departureDate+
                                        "&return_by="+returnDate+"T23%3A00&nonstop=false&currency="+cur+"&number_of_results=1";
                                url = new URL(urlAddress);

                                urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setConnectTimeout(10000);

                                in = urlConnection.getInputStream();

                                isw = new InputStreamReader(in);
                                data = isw.read();
                                response = "";
                                while (data != -1) {
                                    char current = (char) data;
                                    data = isw.read();
                                    response += current;
                                }
                                JSONObject rootObject = new JSONObject(response);


                                //ftiaxnei to object tou ta3idiou
                                final String currency=rootObject.optString("currency");
                                JSONArray results = rootObject.optJSONArray("results");
                                double price = results.optJSONObject(0).optJSONObject("fare").optDouble("total_price");
                                ArrayList<Flight> outbound = new ArrayList<>();
                                JSONArray jsonFlights= results.optJSONObject(0).optJSONArray("itineraries").optJSONObject(0).optJSONObject("outbound").optJSONArray("flights");
                                for(int i =0; i<jsonFlights.length(); i++){
                                    outbound.add(new Flight(jsonFlights.optJSONObject(i).optString("departs_at"),
                                            jsonFlights.optJSONObject(i).optString("arrives_at"),
                                            jsonFlights.optJSONObject(i).optJSONObject("origin").optString("airport"),
                                            jsonFlights.optJSONObject(i).optJSONObject("destination").optString("airport"),
                                            jsonFlights.optJSONObject(i).optJSONObject("booking_info").optInt("seats_remaining")));
                                }
                                ArrayList<Flight> inbound = new ArrayList<>();
                                jsonFlights= results.optJSONObject(0).optJSONArray("itineraries").optJSONObject(0).optJSONObject("inbound").optJSONArray("flights");
                                for(int i =0; i<jsonFlights.length(); i++){
                                    inbound.add(new Flight(jsonFlights.optJSONObject(i).optString("departs_at"),
                                            jsonFlights.optJSONObject(i).optString("arrives_at"),
                                            jsonFlights.optJSONObject(i).optJSONObject("origin").optString("airport"),
                                            jsonFlights.optJSONObject(i).optJSONObject("destination").optString("airport"),
                                            jsonFlights.optJSONObject(i).optJSONObject("booking_info").optInt("seats_remaining")));
                                }

                                final Trip luckyTrip = new Trip(currency, price, outbound, inbound);

                                //pairnei ta onomata ton poleon/aerodromion me vasi tous kodikous
                                urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?&apikey="+
                                        MainActivity.amadeusKey+"&term="+
                                        luckyTrip.getOutboundFlights().get(0).getOrigin();
                                url = new URL(urlAddress);
                                urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setConnectTimeout(10000);
                                in = urlConnection.getInputStream();
                                isw = new InputStreamReader(in);
                                data = isw.read();
                                response = "";
                                while (data != -1) {
                                    char current = (char) data;
                                    data = isw.read();
                                    response += current;
                                }
                                jsonRootObject = new JSONArray(response);
                                jsonObject = jsonRootObject.getJSONObject(0);
                                final String originName=jsonObject.optString("label");

                                urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?&apikey="+
                                        MainActivity.amadeusKey+"&term="+
                                        luckyTrip.getOutboundFlights().get(luckyTrip.getOutboundFlights().size()-1).getDestination();
                                url = new URL(urlAddress);
                                urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setConnectTimeout(10000);
                                in = urlConnection.getInputStream();
                                isw = new InputStreamReader(in);
                                data = isw.read();
                                response = "";
                                while (data != -1) {
                                    char current = (char) data;
                                    data = isw.read();
                                    response += current;
                                }
                                jsonRootObject = new JSONArray(response);
                                jsonObject = jsonRootObject.getJSONObject(0);
                                final String destinationName=jsonObject.optString("label");

                                if(getActivity()!=null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //provoli dedomenon sto UI
                                            TextView luckyTripText = (TextView) getActivity().findViewById(R.id.lucky_trip_text);
                                            if (luckyTripText != null) {
                                                SpannableString spanString = new SpannableString(originName + "\r\nto\r\n" + destinationName);
                                                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                                                luckyTripText.setText(spanString);

                                                LinearLayout luckyTripContainer = (LinearLayout) getActivity().findViewById(R.id.lucky_trip_container);
                                                luckyTripContainer.setVisibility(View.VISIBLE);
                                                luckyTripContainer.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        SearchFragment.showTripDetails(getActivity(), luckyTrip);
                                                    }
                                                });


                                                TextView priceText = (TextView) getActivity().findViewById(R.id.price);
                                                priceText.setText(String.valueOf(luckyTrip.getPrice()) + " " + luckyTrip.getCurrency());

                                                TextView departureDateText = (TextView) getActivity().findViewById(R.id.departure_date);
                                                departureDateText.setText("Leaves at: " + luckyTrip.getOutboundFlights().get(0).getDeparts_at().replace("T", " "));

                                                TextView returnDateText = (TextView) getActivity().findViewById(R.id.return_date);
                                                returnDateText.setText("Returns at: " +
                                                        luckyTrip.getInboundFlights().get(luckyTrip.getInboundFlights().size() - 1).getArrives_at().replace("T", " "));
                                            }

                                        }
                                    });
                                }



                            }catch (UnknownHostException e){
                                //sinithos afto to error emfanizete otan den iparxi sindesi
                                if(getActivity()!=null) {
                                    getActivity().runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            if (HomeFragment.this.isVisible()) {
                                                toast.setText("Check your Internet connection");
                                                toast.show();
                                            }
                                        }
                                    });
                                }

                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                //ektos apo kapio litourgiko error
                                //simvenei kai otan oi sintetagmenes einai poli makria apo kapio aerodromio
                                //px mporei na pareis random sintetagmenes stin mesi tou irinikou
                                //h otan den iparxi diathesimi ptisi gia ta random kritiria
                                retry=true;
                            }
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }

                            //se periptwsi pou den vrethike kapia ptisi me ta random kritiria
                            //3anaprospathei me kenourgia kai efoson o xristis
                            //vriskete akoma sto tab
                            if (retry && !stopThread){
                                if(getActivity()!=null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            logo.performClick();
                                            toast.setText("Sometimes luck is late, be patient!");
                                            toast.show();
                                        }
                                    });
                                }
                            }
                            else
                            {
                                //reset gia na 3anaxrisimopoihthei
                                if(getActivity()!=null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //vazoume null kai stin sinexia
                                            //to 3anarxikopioume wste na mporei na 3anatre3ei to animation
                                            if (logo != null) {
                                                planeAnim.stop();
                                                planeAnim.selectDrawable(0);
                                            }
                                        }
                                    });
                                }

                            }
                        }

                    });
                    newLuckyTrip.start();

                }
                else{
                    toast.setText("Sometimes luck is late, be patient!");
                    toast.show();
                }






            }
        });




        return rootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            stopThread=true;
        }else{
            if(newLuckyTrip.isAlive() && planeAnim != null){
                planeAnim.start();
            }
        }
    }

}

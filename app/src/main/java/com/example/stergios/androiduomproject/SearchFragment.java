package com.example.stergios.androiduomproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class SearchFragment extends android.support.v4.app.Fragment {
    private Toast toast;
    public static boolean isDetailsDialogOn=false;
    public static AlertDialog alert;
    ProgressDialog progressDialog;
    private  String originCode="";
    private String originName="";
    private String destinationCode="";
    private String destinationName="";
    private String currentDay, currentYear;
    private int currentMonth;
    private EditText from, to;
    private CheckBox direct, oneWay;
    private Spinner leavingDay, returnDay;
    int monthsRange=8;

    int currentLeavingMonth=0;
    int currentReturnMonth=0;

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        //arxikopiithite edw kai stin sinexia xrisimopihte
        //to idio gia opiodipote minima oste na apofigoume
        //tin dimiourgia pollwn toast stin sira
        toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);

        from= (EditText) rootView.findViewById(R.id.from);
        to = (EditText) rootView.findViewById(R.id.to);

        direct = (CheckBox) rootView.findViewById(R.id.direct);
        oneWay = (CheckBox) rootView.findViewById(R.id.one_way);

        Button nextLeavingMonth = (Button) rootView.findViewById(R.id.next_leaving_month);
        Button previousLeavingMonth = (Button) rootView.findViewById(R.id.previous_leaving_month);
        Button nextReturnMonth = (Button) rootView.findViewById(R.id.next_return_month);
        Button previousReturnMonth = (Button) rootView.findViewById(R.id.previous_return_month);
        final Button search =(Button) rootView.findViewById(R.id.search);

        leavingDay = (Spinner) rootView.findViewById(R.id.leaving_day);
        returnDay = (Spinner) rootView.findViewById(R.id.return_day);
        final TextView leavingMonth=(TextView) rootView.findViewById(R.id.leaving_month);
        final TextView returnMonth=(TextView) rootView.findViewById(R.id.return_month);

        nextLeavingMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLeavingMonth<monthsRange){
                    currentLeavingMonth++;
                    nameMonth(leavingMonth, currentMonth+currentLeavingMonth);
                    createDays(rootView, leavingDay, currentMonth+currentLeavingMonth);
                }
            }
        });
        nextReturnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentReturnMonth<monthsRange){
                    currentReturnMonth++;
                    nameMonth(returnMonth, currentMonth+currentReturnMonth);
                    createDays(rootView, returnDay, currentMonth+currentReturnMonth);
                }
            }
        });
        previousLeavingMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLeavingMonth>0){
                    currentLeavingMonth--;
                    nameMonth(leavingMonth, currentMonth+currentLeavingMonth);
                    createDays(rootView, leavingDay, currentMonth+currentLeavingMonth);
                }
            }
        });
        previousReturnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentReturnMonth>0){
                    currentReturnMonth--;
                    nameMonth(returnMonth, currentMonth+currentReturnMonth);
                    createDays(rootView, returnDay, currentMonth+currentReturnMonth);
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!oneWay.isChecked()&&((currentReturnMonth<currentLeavingMonth)||(currentLeavingMonth==currentReturnMonth &&
                        Integer.parseInt(leavingDay.getSelectedItem().toString())>Integer.parseInt(returnDay.getSelectedItem().toString())))){
                    toast.setText("You can't return before you leave!");
                    toast.show();
                }
                else if(!(from.getText().length() >2 && to.getText().length()>2)){
                    toast.setText("Use at least 3 characters for city names");
                    toast.show();
                }else{
                    search();
                }
            }
        });

        createCalendar(rootView);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    void createCalendar(View view){

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd");
        currentDay= df.format(c.getTime());
        df = new SimpleDateFormat("MM");
        currentMonth= Integer.parseInt(df.format(c.getTime()));
        df= new SimpleDateFormat("y");
        currentYear=df.format(c.getTime());

        Spinner leavingDay = (Spinner) view.findViewById(R.id.leaving_day);
        Spinner returnDay = (Spinner) view.findViewById(R.id.return_day);
        TextView leavingMonth=(TextView) view.findViewById(R.id.leaving_month);
        TextView returnMonth=(TextView) view.findViewById(R.id.return_month);

        nameMonth(leavingMonth, currentMonth);
        nameMonth(returnMonth, currentMonth);
        createDays(view, leavingDay, currentMonth);
        createDays(view, returnDay, currentMonth);
    }

    void createDays(View view, Spinner days, int month){
        int numberOfDays;

        if(month==1||month==3||month==5||month==7||month==8||month==10||month==12){
            numberOfDays=31;
        }else if(month==2)
            numberOfDays=28;
        else {
            numberOfDays=30;
        }

        ArrayList<String> daysArray = new ArrayList<>();
        for (int i=0; i<numberOfDays;i++){
            if(month==currentMonth) {
                if(i+1>=Integer.parseInt(currentDay)) {
                    if(i<9) {
                        daysArray.add("0"+Integer.toString(i + 1));
                    }
                    else{
                        daysArray.add(Integer.toString(i + 1));
                    }
                }
            }else{
                if(i<9) {
                    daysArray.add("0"+Integer.toString(i + 1));
                }
                else{
                    daysArray.add(Integer.toString(i + 1));
                }
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, daysArray);
        days.setAdapter(spinnerArrayAdapter);


    }

    void nameMonth(TextView view, int month){
        switch (month){
            case 1:
                view.setText("Jan");
                break;
            case 2:
                view.setText("Feb");
                break;
            case 3:
                view.setText("Mar");
                break;
            case 4:
                view.setText("Apr");
                break;
            case 5:
                view.setText("May");
                break;
            case 6:
                view.setText("Jun");
                break;
            case 7:
                view.setText("Jul");
                break;
            case 8:
                view.setText("Aug");
                break;
            case 9:
                view.setText("Sep");
                break;
            case 10:
                view.setText("Oct");
                break;
            case 11:
                view.setText("Nov");
                break;
            case 12:
                view.setText("Dec");
                break;

        }


    }


    void search(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setMessage("Searching...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                        }
                    });
                }

                boolean cityNameOK = false;
                HttpURLConnection urlConnection=null;
                try{
                    String urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey="+MainActivity.amadeusKey+
                            "&term="+urlCoder(from.getText().toString());
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
                    originCode = jsonObject.optString("value");
                    originName = jsonObject.optString("label");

                    urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey="+MainActivity.amadeusKey+
                            "&term="+urlCoder(to.getText().toString());
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
                    destinationCode = jsonObject.optString("value");
                    destinationName = jsonObject.optString("label");


                    cityNameOK = true;
                }catch (UnknownHostException e){
                    //sinithos afto to error emfanizete otan den iparxi sindesi
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (SearchFragment.this.isMenuVisible()) {
                                    toast.setText("Check your Internet connection");
                                    toast.show();
                                }
                            }
                        });
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    if(getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(SearchFragment.this.isMenuVisible()){
                                    toast.setText("Wrong city name");
                                    toast.show();
                                }
                            }
                        });
                    }

                }
                if (cityNameOK){
                    try{
                        //pernei to prokathorismeno nomisma
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String cur = prefs.getString(getString(R.string.pref_cur_key), getResources().getString(R.string.pref_cur_eur));

                        String dMonth;
                        if(currentMonth+currentLeavingMonth<10) {
                            dMonth = "-0" + (currentLeavingMonth + currentMonth)+"-";
                        }
                        else{
                            dMonth = "-"+(currentLeavingMonth + currentMonth)+"-";
                        }

                        String  rMonth;
                        if(currentMonth+currentLeavingMonth<10) {
                            rMonth = "-0" + (currentReturnMonth + currentMonth)+"-";
                        }
                        else{
                            rMonth = "-"+(currentReturnMonth + currentMonth)+"-";
                        }

                        String departureDate = currentYear+dMonth+leavingDay.getSelectedItem().toString();
                        String returnDate = "&return_date="+currentYear+rMonth+returnDay.getSelectedItem().toString();
                        if(oneWay.isChecked()) returnDate="";
                        String noneStop ="";
                        if(direct.isChecked()) noneStop="&nonstop=true";
                        //anazitei ptisi
                        String urlAddress="http://api.sandbox.amadeus.com/v1.2/flights/low-fare-search?apikey="+
                                MainActivity.amadeusKey+"&origin="+originCode+"&destination="+
                                destinationCode+"&departure_date="+departureDate+
                                returnDate+"&currency="+cur+noneStop+"&number_of_results=10";
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
                        JSONObject rootObject = new JSONObject(response);

                        //ftiaxnei tin lista me ta ta3idia
                        final String currency=rootObject.optString("currency");
                        JSONArray results = rootObject.optJSONArray("results");
                        JSONArray itineraries;
                        final ArrayList<Trip> trips = new ArrayList<>();
                        final ArrayList<String> description = new ArrayList<>();
                        JSONArray jsonFlights;
                        ArrayList<Flight> outbound;
                        ArrayList<Flight> inbound;
                        double price;



                        for(int r =0; r< results.length(); r++ ){
                            price = results.optJSONObject(r).optJSONObject("fare").optDouble("total_price");
                            itineraries = results.optJSONObject(r).optJSONArray("itineraries");
                            for(int j =0; j<itineraries.length(); j++){
                                description.add(price+" "+cur);
                                outbound = new ArrayList<>();
                                jsonFlights= itineraries.optJSONObject(j).optJSONObject("outbound").optJSONArray("flights");
                                for(int i =0; i<jsonFlights.length(); i++){
                                    outbound.add(new Flight(jsonFlights.optJSONObject(i).optString("departs_at"),
                                            jsonFlights.optJSONObject(i).optString("arrives_at"),
                                            jsonFlights.optJSONObject(i).optJSONObject("origin").optString("airport"),
                                            jsonFlights.optJSONObject(i).optJSONObject("destination").optString("airport"),
                                            jsonFlights.optJSONObject(i).optJSONObject("booking_info").optInt("seats_remaining")));
                                }
                                inbound = new ArrayList<>();
                                if(!oneWay.isChecked()){
                                    jsonFlights= itineraries.optJSONObject(j).optJSONObject("inbound").optJSONArray("flights");
                                    for(int i =0; i<jsonFlights.length(); i++){
                                        inbound.add(new Flight(jsonFlights.optJSONObject(i).optString("departs_at"),
                                                jsonFlights.optJSONObject(i).optString("arrives_at"),
                                                jsonFlights.optJSONObject(i).optJSONObject("origin").optString("airport"),
                                                jsonFlights.optJSONObject(i).optJSONObject("destination").optString("airport"),
                                                jsonFlights.optJSONObject(i).optJSONObject("booking_info").optInt("seats_remaining")));
                                    }
                                }
                                trips.add(new Trip(currency, price, outbound, inbound));
                            }
                        }



                        if(getActivity()!=null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView tripText = (TextView) getActivity().findViewById(R.id.trip_text);
                                    SpannableString spanString = new SpannableString(originName + "\r\nto\r\n" + destinationName);
                                    spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                                    tripText.setText(spanString);

                                    ListView resultsList = (ListView) getActivity().findViewById(R.id.search_results);
                                    ResultsListAdapter adapter = new ResultsListAdapter(getActivity(), description, trips);
                                    resultsList.setAdapter(adapter);
                                    listViewHeightBasedOnItems(resultsList);
                                }
                            });
                        }

                    }catch (UnknownHostException e){
                        //sinithos afto to error emfanizete otan den iparxi sindesi
                        if(getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (SearchFragment.this.isMenuVisible()) {
                                        toast.setText("Check your Internet connection");
                                        toast.show();
                                    }
                                }
                            });
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        if(getActivity()!=null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(SearchFragment.this.isMenuVisible()){
                                        toast.setText("No flights found");
                                        toast.show();
                                    }
                                }
                            });
                        }

                    }
                }


                if(urlConnection!=null) urlConnection.disconnect();

                if(getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }

            }
        }).start();


    }

    class ResultsListAdapter extends ArrayAdapter<String> {

        private  ArrayList<String> descriptions = new ArrayList<>();
        private ArrayList<Trip> trips = new ArrayList<>();


        ResultsListAdapter(Activity context, ArrayList<String> descriptions, ArrayList<Trip> trips) {
            super(context, R.layout.destionations_list_item, descriptions);
            this.descriptions=descriptions;
            this.trips=trips;

        }

        public View getView(int position,View view,ViewGroup parent) {


            View rowView;
            LayoutInflater inflater=getActivity().getLayoutInflater();

            rowView = inflater.inflate(R.layout.results_list_item, null, true);

            final Trip trip = trips.get(position);

            TextView priceText = (TextView) rowView.findViewById(R.id.results_price);
            priceText.setText(String.valueOf(trip.getPrice()) + " " + trip.getCurrency());

            TextView departureDateText = (TextView) rowView.findViewById(R.id.results_departure_date);
            departureDateText.setText("Leaves at: " + trip.getOutboundFlights().get(0).getDeparts_at().replace("T", " "));

            TextView returnDateText = (TextView) rowView.findViewById(R.id.results_return_date);
            if(trip.getInboundFlights().size()>0) {

                returnDateText.setText("Returns at: " +
                        trip.getInboundFlights().get(trip.getInboundFlights().size() - 1).getArrives_at().replace("T", " "));
            }else{
                returnDateText.setVisibility(View.GONE);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTripDetails(getActivity(), trip);
                }
            });

            return rowView;

        }

    }

    public static void showTripDetails(final Activity c, final Trip trip){
        final ArrayList<String> descriptions= new ArrayList<>();
        isDetailsDialogOn=true;
        final View v = View.inflate(c, R.layout.trip_details, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.TripDialogStyle);
        builder .setView(v)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        isDetailsDialogOn=false;
                    }
                });
        alert = builder.create();

        alert.show();
        resizeDialog(c);


        //na fevgei o dialogos otan ginete click apo ton xristi
        View container =v.findViewById(R.id.details_container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.cancel();
            }
        });



        class DetailsListAdapter extends ArrayAdapter<String> {

            private  ArrayList<String> descriptions = new ArrayList<>();
            private ArrayList<Flight> flights= new ArrayList<>();
            private Activity c;


            DetailsListAdapter(ArrayList<String> descriptions, ArrayList<Flight> flights, Activity c) {
                super(c, R.layout.destionations_list_item, descriptions);
                this.descriptions=descriptions;
                this.flights=flights;
                this.c=c;

            }

            public View getView(int position,View view,ViewGroup parent) {


                View rowView;
                LayoutInflater inflater= c.getLayoutInflater();

                rowView = inflater.inflate(R.layout.details_list_item, null, true);

                Flight flight= flights.get(position);

                TextView descriptionText = (TextView) rowView.findViewById(R.id.details_flight);
                descriptionText.setText(descriptions.get(position));

                TextView departureDateText = (TextView) rowView.findViewById(R.id.details_departs);
                departureDateText.setText("Departs at: " + flight.getDeparts_at().replace("T", " "));

                TextView arrivesDateText = (TextView) rowView.findViewById(R.id.details_arrives);
                arrivesDateText.setText("Arrives at: " + flight.getArrives_at().replace("T", " "));

                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.cancel();
                    }
                });

                return rowView;

            }
            public ArrayList<String> getDescriptions(){
                return descriptions;
            }

        }

        final TextView price =(TextView) v.findViewById(R.id.details_price);
        price.setText("Fetching details...");


        final Button buy =(Button) v.findViewById(R.id.buy);
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, BuyActivity.class);
                Bundle bundle= new Bundle();
                bundle.putString("trip", new Gson().toJson(trip));
                bundle.putStringArrayList("descriptions", descriptions);
                intent.putExtras(bundle);
                c.startActivity(intent);
                alert.cancel();
            }
        });



        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> outDesc= getDescriptionsFromFlights(trip.getOutboundFlights());
                ArrayList<String> inDesc= getDescriptionsFromFlights(trip.getInboundFlights());
                for(String s: outDesc){
                    s="Outbound\r\n"+s;
                    descriptions.add(s);
                }
                for(String s: inDesc){
                    s="Inbound\r\n"+s;
                    descriptions.add(s);
                }

                final ArrayList<Flight> allFlights = trip.getOutboundFlights();
                for(Flight f: trip.getInboundFlights()){
                    allFlights.add(f);
                }


                if(v!=null){
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            price.setText(trip.getPrice()+" "+trip.getCurrency());
                            DetailsListAdapter outAdapter= new DetailsListAdapter(descriptions, allFlights, c);
                            ListView fList= (ListView) v.findViewById(R.id.detailed_flights_list);
                            fList.setAdapter(outAdapter);
                            v.findViewById(R.id.buy).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    public static void resizeDialog(Activity c){
        //pernoume tis diastaseis tis othonis
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        //kai ftiaxnoume ton dialogo ligo mikrotero
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alert.getWindow().getAttributes());

        int margin =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                c.getResources().getDimension(R.dimen.activity_horizontal_margin),
                c.getResources().getDisplayMetrics());
        lp.height=height;
        lp.width=width-2*margin;
        alert.getWindow().setAttributes(lp);
    }

    public static ArrayList<String> getDescriptionsFromFlights(final ArrayList<Flight> flights){
        final ArrayList<String> descriptions = new ArrayList<>();

        HttpURLConnection urlConnection=null;
        try {
            String urlAddress;
            InputStream in;
            InputStreamReader isw;
            int data;
            String response;
            JSONArray jsonRootObject;
            JSONObject jsonObject;
            String origin="";
            String destination="";

            for(Flight f: flights){
                urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey="+MainActivity.amadeusKey+
                        "&term="+f.getOrigin();
                URL url = new URL(urlAddress);

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

                origin= jsonObject.optString("label");

                urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey="+MainActivity.amadeusKey+
                        "&term="+f.getDestination();
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

                destination= jsonObject.optString("label");

                descriptions.add(origin + "\r\nto\r\n" + destination);

            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(urlConnection!=null) urlConnection.disconnect();

        return descriptions;
    }

    String urlCoder(String url){
        String encodedurl="";
        try {
            url=url.split("\\[")[0].trim();
            encodedurl = URLEncoder.encode(url,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedurl;
    }

    ViewGroup.LayoutParams listViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();


            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++){
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0,0);
                totalItemsHeight += item.getMeasuredHeight();


            }


            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);


            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;

            return params;

        } else {
            return null;
        }

    }


}
package com.example.stergios.androiduomproject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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


public class SuggestionsFragment extends android.support.v4.app.Fragment {
    private AnimationDrawable loadingAnim;
    static Thread getDestinationsThread= new Thread();
    private ArrayList<String> topDestinationNames= new ArrayList<>();
    private ListView suggestionsList;
    private DestinationsListAdapter adapter;
    private ImageView loadingImage;
    private String originCity ="";
    private Toast toast;
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_suggestions, container,
                false);

        //arxikopiithite edw kai stin sinexia xrisimopihte
        //to idio gia opiodipote minima oste na apofigoume
        //tin dimiourgia pollwn toast stin sira
        toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);

        loadingImage = (ImageView) rootView.findViewById(R.id.loading_view);
        loadingAnim=new AnimationDrawable();
        for(int i=0; i<15;i++){
            try {
                InputStream ims = getActivity().getAssets().open("loading_frames/"+i+".gif");
                Drawable d = Drawable.createFromStream(ims, null);
                loadingAnim.addFrame(d, 50);
            }
            catch(Exception e){e.printStackTrace();}
        }
        loadingAnim.setOneShot(false);
        loadingImage.setBackgroundDrawable(loadingAnim);
        if(!getDestinationsThread.isAlive())
            getTopDestination();

        loadingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!getDestinationsThread.isAlive())
                    getTopDestination();
            }
        });


        return rootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefOriginCity = prefs.getString(getString(R.string.pref_location_code_key), getString(R.string.pref_location_code_default));
        if(!prefOriginCity.equals(originCity)){
            getTopDestination();
        }
    }



    void getTopDestination(){
        getDestinationsThread= new Thread(new Runnable() {
            @Override
            public void run() {

                if(SuggestionsFragment.this.getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            loadingAnim.start();
                        }
                    });
                }

                HttpURLConnection urlConnection=null;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    originCity = prefs.getString(getString(R.string.pref_location_code_key), getString(R.string.pref_location_code_default));

                    //dimiourgi imerominia 1 xrono prin wste na paroume
                    //tous top proorismous gia aftin tin epoxi
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("y-MM");
                    c.add(Calendar.YEAR, -1);
                    String period= df.format(c.getTime());
                    String urlAddress="https://api.sandbox.amadeus.com/v1.2/travel-intelligence/top-destinations?apikey="+
                            MainActivity.amadeusKey+"&period="+period+"&origin="+ originCity +"&number_of_results=20";
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



                    topDestinationNames= new ArrayList<>();

                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                suggestionsList = (ListView) getActivity().findViewById(R.id.suggestions_list);
                                if (suggestionsList != null) {
                                    adapter = new DestinationsListAdapter(getActivity(), topDestinationNames);
                                    suggestionsList.setAdapter(adapter);
                                }
                            }
                        });
                    }
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray topDestinationsJSONArray= jsonObject.optJSONArray("results");

                    //gia kathe kodiko proorismoy pernoume tin perigrafi/onoma
                    for(int i=0;i<topDestinationsJSONArray.length();i++){
                        urlAddress="https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?apikey="+MainActivity.amadeusKey+
                                "&term="+topDestinationsJSONArray.optJSONObject(i).optString("destination");
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

                        JSONArray jsonRootObject = new JSONArray(response);

                        jsonObject = jsonRootObject.getJSONObject(0);

                        final String label = jsonObject.optString("label");
                        if(SuggestionsFragment.this.getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (suggestionsList != null) {
                                        if( suggestionsList.getAdapter() !=null) {
                                            topDestinationNames.add(label);
                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                }
                            });
                        }



                    }


                }catch (UnknownHostException e){
                    //sinithos afto to error emfanizete otan den iparxi sindesi
                    if(SuggestionsFragment.this.getActivity()!=null) {
                        SuggestionsFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (loadingImage != null) {
                                    loadingAnim.stop();

                                }
                                if (SuggestionsFragment.this.isMenuVisible()) {

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
                                if (loadingImage != null) {
                                    loadingAnim.stop();
                                }
                                if(SuggestionsFragment.this.isMenuVisible()){

                                    toast.setText("Something went wrong :(");
                                    toast.show();
                                }
                            }
                        });
                    }

                }

                if(urlConnection!=null) urlConnection.disconnect();

                if(getActivity()!=null && suggestionsList!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingImage != null) {
                                loadingAnim.stop();
                                if (topDestinationNames.size()>0)
                                    loadingImage.setVisibility(View.GONE);
                            }

                        }
                    });
                }

            }
        });
        getDestinationsThread.start();
    }


    class DestinationsListAdapter extends ArrayAdapter<String> {

        private  ArrayList<String> descriptions = new ArrayList<>();


        DestinationsListAdapter(Activity context, ArrayList<String> descriptions) {
            super(context, R.layout.destionations_list_item, descriptions);
            this.descriptions=descriptions;

        }

        public View getView(final int position, View view, ViewGroup parent) {


            View rowView;
            LayoutInflater inflater=getActivity().getLayoutInflater();

            rowView = inflater.inflate(R.layout.destionations_list_item, null, true);

            TextView description= (TextView) rowView.findViewById(R.id.description);
            description.setText(descriptions.get(position));

            ImageButton startSearch = (ImageButton) rowView.findViewById(R.id.start_search);
            startSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getActivity() instanceof MainActivity){
                        ((MainActivity) getActivity()).startSearchFromSuggestions(descriptions.get(position));
                    }
                }
            });
            return rowView;

        }

    }




}
package com.example.stergios.androiduomproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class BuyActivity extends AppCompatActivity {

    private Trip trip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        String gsonTrip=getIntent().getExtras().getString("trip");
        trip=new Gson().fromJson(gsonTrip, Trip.class);
        ArrayList<String> descriptions=getIntent().getExtras().getStringArrayList("descriptions");

        TextView price=(TextView) findViewById(R.id.price);
        price.setText(String.valueOf(trip.getPrice())+" "+trip.getCurrency());

        ArrayList<Flight> allFlights=new ArrayList<>();
        allFlights.addAll(trip.getOutboundFlights());
        allFlights.addAll(trip.getInboundFlights());

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.buy_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        TripAdapter mAdapter=new TripAdapter(this, allFlights, descriptions);
        mRecyclerView.setAdapter(mAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return true;

        }


    }

    public class TripAdapter extends RecyclerView.Adapter<TripAdapter.FlightHolder> {
        private ArrayList<String> descriptions;
        private ArrayList<Flight> flights;
        private Context context;
        public TripAdapter(Context context,ArrayList<Flight> flights, ArrayList<String> descriptions){
            this.flights=flights;
            this.context=context;
            this.descriptions=descriptions;
        }

        @Override
        public FlightHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.buy_list_item, null);
            FlightHolder flightHolder = new FlightHolder(v);

            return flightHolder;
        }

        @Override
        public void onBindViewHolder(FlightHolder flightHolder, int i) {
            Flight flight = flights.get(i);
            flightHolder.flight.setText(descriptions.get(i));
            flightHolder.departs.setText("Departs at: " + flight.getDeparts_at().replace("T", " "));
            flightHolder.arrives.setText("Arrives at: " + flight.getArrives_at().replace("T", " "));
            flightHolder.remaining_seats.setText("Remaining Seats: "+String.valueOf(flight.getRemainingSeats()));
        }

        @Override
        public int getItemCount() {
            return descriptions.size();
        }

        public class FlightHolder extends RecyclerView.ViewHolder {
            protected TextView flight;
            protected TextView departs;
            protected TextView arrives;
            protected TextView remaining_seats;


            public FlightHolder(View v) {
                super(v);
                this.flight=(TextView) v.findViewById(R.id.buy_flight);
                this.departs=(TextView) v.findViewById(R.id.buy_departs);
                this.arrives=(TextView) v.findViewById(R.id.buy_arrives);
                this.remaining_seats=(TextView) v.findViewById(R.id.buy_seats);

                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                int width = windowManager.getDefaultDisplay().getWidth();
                v.setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.MATCH_PARENT));
            }
        }


    }

}

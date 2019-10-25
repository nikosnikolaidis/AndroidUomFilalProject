package com.example.stergios.androiduomproject;

import java.util.ArrayList;


public class Trip {
    private String currency;
    private double price;
    private ArrayList<Flight> outboundFlights, inboundFlights;

    public Trip(String currency, double price, ArrayList<Flight> outboundFlights, ArrayList<Flight> inboundFlights) {
        this.currency = currency;
        this.price = price;
        this.outboundFlights = outboundFlights;
        this.inboundFlights = inboundFlights;
    }

    public String getCurrency() {
        return currency;
    }

    public double getPrice() {
        return price;
    }

    public ArrayList<Flight> getOutboundFlights() {
        return outboundFlights;
    }

    public ArrayList<Flight> getInboundFlights() {
        return inboundFlights;
    }


}

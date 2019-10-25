package com.example.stergios.androiduomproject;


public class Flight{
    private String departs_at;
    private String arrives_at;
    private String origin;
    private String destination;
    private int remainingSeats;

    public Flight(String departs_at,String arrives_at,String origin,String destination, int remainingSeats){
        this.departs_at=departs_at;
        this.arrives_at=arrives_at;
        this.origin=origin;
        this.destination=destination;
        this.remainingSeats=remainingSeats;
    }

    public String getDeparts_at() {
        return departs_at;
    }

    public String getArrives_at() {
        return arrives_at;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public int getRemainingSeats() {
        return remainingSeats;
    }

}

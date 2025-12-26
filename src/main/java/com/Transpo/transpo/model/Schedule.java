package com.Transpo.transpo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Bus bus;
    @ManyToOne(optional = false)
    private Route route;

    private LocalDateTime departureTime;

    private double fare;

    //available seats to book (keeps updated)
    private int availableSeats;

    public Schedule() {
    }

    public Schedule(Bus bus, Route route, LocalDateTime departureTime, double fare, int availableSeats) {
        this.bus = bus;
        this.route = route;
        this.departureTime = departureTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
    }

    //getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bus getBus(){return bus; }
    public void setBus(Bus bus){ this.bus = bus; }

    public Route getRoute(){ return route; }
    public void setRoute(Route route){ this.route = route; }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

}

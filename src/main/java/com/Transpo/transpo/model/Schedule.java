package com.Transpo.transpo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)  // Change to EAGER loading
    private Bus bus;
    
    @ManyToOne(optional = false, fetch = FetchType.EAGER)  // Change to EAGER loading
    private Route route;

    private LocalDateTime departureTime;
    private double fare;
    private int availableSeats;
    
    // Add transient fields for direct ID access
    @Transient
    private Long busId;
    
    @Transient
    private Long routeId;

    public Schedule() {
    }

    public Schedule(Bus bus, Route route, LocalDateTime departureTime, double fare, int availableSeats) {
        this.bus = bus;
        this.route = route;
        this.departureTime = departureTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { 
        this.bus = bus; 
        if (bus != null) {
            this.busId = bus.getId();
        }
    }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { 
        this.route = route; 
        if (route != null) {
            this.routeId = route.getId();
        }
    }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    // Helper methods for IDs
    public Long getBusId() {
        if (bus != null) return bus.getId();
        return busId;
    }
    
    public void setBusId(Long busId) {
        this.busId = busId;
    }
    
    public Long getRouteId() {
        if (route != null) return route.getId();
        return routeId;
    }
    
    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
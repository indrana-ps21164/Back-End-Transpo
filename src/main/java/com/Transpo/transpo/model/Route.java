package com.Transpo.transpo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String origin;

    @Column(nullable=false)
    private String destination;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @OrderBy("sequence ASC")
    private List<BusStop> busStops = new ArrayList<>();

    public Route() {}

    public Route(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }


    public List<BusStop> getBusStops() { return busStops; }
    public void setBusStops(List<BusStop> busStops) { this.busStops = busStops; }
    
    // Helper method to add bus stop
    public void addBusStop(BusStop busStop) {
        busStops.add(busStop);
        busStop.setRoute(this);
    }
    
    // Helper method to remove bus stop
    public void removeBusStop(BusStop busStop) {
        busStops.remove(busStop);
        busStop.setRoute(null);
    }
}
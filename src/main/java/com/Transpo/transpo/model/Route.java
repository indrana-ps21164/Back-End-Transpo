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

    // Optional named stops Stop01 .. Stop10
    private String stop01;
    private String stop02;
    private String stop03;
    private String stop04;
    private String stop05;
    private String stop06;
    private String stop07;
    private String stop08;
    private String stop09;
    private String stop10;

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

    public String getStop01() { return stop01; }
    public void setStop01(String stop01) { this.stop01 = stop01; }

    public String getStop02() { return stop02; }
    public void setStop02(String stop02) { this.stop02 = stop02; }

    public String getStop03() { return stop03; }
    public void setStop03(String stop03) { this.stop03 = stop03; }

    public String getStop04() { return stop04; }
    public void setStop04(String stop04) { this.stop04 = stop04; }

    public String getStop05() { return stop05; }
    public void setStop05(String stop05) { this.stop05 = stop05; }

    public String getStop06() { return stop06; }
    public void setStop06(String stop06) { this.stop06 = stop06; }

    public String getStop07() { return stop07; }
    public void setStop07(String stop07) { this.stop07 = stop07; }

    public String getStop08() { return stop08; }
    public void setStop08(String stop08) { this.stop08 = stop08; }

    public String getStop09() { return stop09; }
    public void setStop09(String stop09) { this.stop09 = stop09; }

    public String getStop10() { return stop10; }
    public void setStop10(String stop10) { this.stop10 = stop10; }


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
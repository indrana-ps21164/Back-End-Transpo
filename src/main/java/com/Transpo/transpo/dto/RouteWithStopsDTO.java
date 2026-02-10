package com.Transpo.transpo.dto;

import java.util.List;

public class RouteWithStopsDTO {
    private Long id;
    private String name;
    private String origin;
    private String destination;
    private List<BusStopDTO> stops;

    public RouteWithStopsDTO() {}

    public RouteWithStopsDTO(Long id, String name, String origin, String destination, List<BusStopDTO> stops) {
        this.id = id;
        this.name = name;
        this.origin = origin;
        this.destination = destination;
        this.stops = stops;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public List<BusStopDTO> getStops() { return stops; }
    public void setStops(List<BusStopDTO> stops) { this.stops = stops; }
}
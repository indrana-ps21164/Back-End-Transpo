package com.Transpo.transpo.dto;

import java.util.List;
import java.util.Map;

public class MapDataDTO {
    private RouteInfoDTO route;
    private List<StopInfoDTO> stops;
    private PassengerStatsDTO passengerStats;
    
    public MapDataDTO() {}
    
    // Getters and Setters
    public RouteInfoDTO getRoute() { return route; }
    public void setRoute(RouteInfoDTO route) { this.route = route; }
    
    public List<StopInfoDTO> getStops() { return stops; }
    public void setStops(List<StopInfoDTO> stops) { this.stops = stops; }
    
    public PassengerStatsDTO getPassengerStats() { return passengerStats; }
    public void setPassengerStats(PassengerStatsDTO passengerStats) { this.passengerStats = passengerStats; }
    
    // Inner DTO classes
    public static class RouteInfoDTO {
        private String origin;
        private String destination;
        private Map<String, Double> originCoords;
        private Map<String, Double> destinationCoords;
        
        // Getters and Setters
        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        
        public Map<String, Double> getOriginCoords() { return originCoords; }
        public void setOriginCoords(Map<String, Double> originCoords) { this.originCoords = originCoords; }
        
        public Map<String, Double> getDestinationCoords() { return destinationCoords; }
        public void setDestinationCoords(Map<String, Double> destinationCoords) { this.destinationCoords = destinationCoords; }
    }
    
    public static class StopInfoDTO {
        private Long id;
        private String name;
        private Double lat;
        private Double lng;
        private Integer sequence;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
        
        public Integer getSequence() { return sequence; }
        public void setSequence(Integer sequence) { this.sequence = sequence; }
    }
    
    public static class PassengerStatsDTO {
        private Map<String, Object> route;
        private List<Map<String, Object>> stops;
        private Integer totalPassengers;
        
        // Getters and Setters
        public Map<String, Object> getRoute() { return route; }
        public void setRoute(Map<String, Object> route) { this.route = route; }
        
        public List<Map<String, Object>> getStops() { return stops; }
        public void setStops(List<Map<String, Object>> stops) { this.stops = stops; }
        
        public Integer getTotalPassengers() { return totalPassengers; }
        public void setTotalPassengers(Integer totalPassengers) { this.totalPassengers = totalPassengers; }
    }
}
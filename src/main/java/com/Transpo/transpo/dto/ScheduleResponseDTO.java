package com.Transpo.transpo.dto;

import java.time.LocalDateTime;

public class ScheduleResponseDTO {
    private Long id;
    private Long busId;
    private String busNumber;
    private Long routeId;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private double fare;
    private int availableSeats;

    // Constructor
    public ScheduleResponseDTO(Long id, Long busId, String busNumber, Long routeId, 
                              String origin, String destination, LocalDateTime departureTime, 
                              double fare, int availableSeats) {
        this.id = id;
        this.busId = busId;
        this.busNumber = busNumber;
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
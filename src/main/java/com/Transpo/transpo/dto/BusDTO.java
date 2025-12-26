package com.Transpo.transpo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BusDTO {
    
    private Long id;
    @NotBlank
    private String busNumber;
    @NotBlank
    private String busName;
    @Min(1)
    private int totalSeats;

    public BusDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
}

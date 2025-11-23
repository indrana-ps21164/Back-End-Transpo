package com.Transpo.transpo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "buses")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String busNumber;

    private String busName;

    private int totalSeats;

    public Bus() {
    }

    public Bus(String busNumber, String busName, int totalSeats) {
        this.busNumber = busNumber;
        this.busName = busName;
        this.totalSeats = totalSeats;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getBusNumber() {
        return busNumber;
    }
    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusName() {
        return busName;
    }
    public void setBusName(String busName) {
        this.busName = busName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
}

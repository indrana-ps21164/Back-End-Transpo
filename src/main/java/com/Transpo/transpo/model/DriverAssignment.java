package com.Transpo.transpo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_assignments")
public class DriverAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "driver_id", unique = true, nullable = false)
    private User driver;
    
    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;
    
    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    public DriverAssignment() {}
    
    public DriverAssignment(User driver, Bus bus) {
        this.driver = driver;
        this.bus = bus;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }
    
    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
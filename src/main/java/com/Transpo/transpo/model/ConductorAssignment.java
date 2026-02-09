package com.Transpo.transpo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conductor_assignments")
public class ConductorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "conductor_id", unique = true, nullable = false)
    private User conductor;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    private Instant assignedAt = Instant.now();

    public ConductorAssignment() {
    }

    public ConductorAssignment(User conductor, Bus bus) {
        this.conductor = conductor;
        this.bus = bus;
        this.assignedAt = Instant.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getConductor() { return conductor; }
    public void setConductor(User conductor) { this.conductor = conductor; }

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
}

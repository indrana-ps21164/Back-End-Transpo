package com.Transpo.transpo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Schedule schedule;

    private String passengerName;
    private String passengerEmail;
    private int seatNumber;

    private LocalDateTime bookingTime = LocalDateTime.now();

    public Reservation() {
    }

    public Reservation(Schedule schedule, String passengerName, String passengerEmail, int seatNumber) {
        this.schedule = schedule;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.seatNumber = seatNumber;
    }

    //getters and setters

    public Long getId() {
        return id;
    }
    public void setId(Long id){this.id = id;}

    public Schedule getSchedule(){return schedule;}
    public void setSchedule(Schedule schedule){ this.schedule = schedule; }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}

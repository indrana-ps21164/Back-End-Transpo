package com.Transpo.transpo.dto;

import java.time.LocalDateTime;

public class ReservationDTO {
    private Long id;
    private Long scheduleId;
    private String passengerName;
    private String passengerEmail;
    private int seatNumber;
    private LocalDateTime bookingTime;
    private Long pickupStopId;  // New field
    private Long dropStopId;    // New field
    private String username; // logged-in username of passenger


    public ReservationDTO() {}

    // getters/setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public Long getPickupStopId() { return pickupStopId; }
    public void setPickupStopId(Long pickupStopId) { this.pickupStopId = pickupStopId; }

    public Long getDropStopId() { return dropStopId; }
    public void setDropStopId(Long dropStopId) { this.dropStopId = dropStopId; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

package com.Transpo.transpo.dto;

import java.util.List;

public class SeatAvailabilityDTO {
    public static class Seat {
        public int seatNumber;
        public String status; // AVAILABLE | RESERVED | PAID
        public String passengerName; // only for admin/conductor
    }

    private Long busId;
    private String busNumber;
    private Long scheduleId;
    private int totalSeats;
    private List<Seat> seats;

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
}
package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.ReservationDTO;
import com.Transpo.transpo.model.Reservation;

public class ReservationMapper {

    public static ReservationDTO toDto(Reservation r) {
        if (r == null) return null;
        ReservationDTO d = new ReservationDTO();
        d.setId(r.getId());
        d.setScheduleId(r.getSchedule() != null ? r.getSchedule().getId() : null);
        d.setPassengerName(r.getPassengerName());
        d.setPassengerEmail(r.getPassengerEmail());
        d.setSeatNumber(r.getSeatNumber());
        d.setBookingTime(r.getBookingTime());
        d.setPickupStopId(r.getPickupStop() != null ? r.getPickupStop().getId() : null);
        d.setDropStopId(r.getDropStop() != null ? r.getDropStop().getId() : null);
    d.setPickup(r.getPickupStop() != null ? r.getPickupStop().getName() : null);
    d.setDrop(r.getDropStop() != null ? r.getDropStop().getName() : null);
    d.setUsername(r.getUsername());
    d.setBusNumber(r.getSchedule() != null && r.getSchedule().getBus() != null ? r.getSchedule().getBus().getBusNumber() : null);
    d.setDepartureTime(r.getSchedule() != null ? r.getSchedule().getDepartureTime() : null);
    d.setStatus(r.isPaid() ? "PAID" : "RESERVED");
        return d;
    }
}

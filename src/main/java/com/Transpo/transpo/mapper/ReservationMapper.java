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
        return d;
    }
}

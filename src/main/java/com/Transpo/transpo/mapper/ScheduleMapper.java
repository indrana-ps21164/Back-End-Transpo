package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.ScheduleDTO;
import com.Transpo.transpo.model.Schedule;

public class ScheduleMapper {

    public static ScheduleDTO toDto(Schedule s) {
        if (s == null) return null;
        ScheduleDTO d = new ScheduleDTO();
        d.setId(s.getId());
        if (s.getBus() != null) {
            d.setBusId(s.getBus().getId());
            d.setBusNumber(s.getBus().getBusNumber());
        }
        if (s.getRoute() != null) {
            d.setRouteId(s.getRoute().getId());
            d.setOrigin(s.getRoute().getOrigin());
            d.setDestination(s.getRoute().getDestination());
        }
        d.setDepartureTime(s.getDeparturTime());
        d.setFare(s.getFare());
        d.setAvailableSeats(s.getAvailableSeats());
        return d;
    }
}

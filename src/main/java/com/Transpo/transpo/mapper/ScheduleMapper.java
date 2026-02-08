package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.ScheduleDTO;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.BusRepository;
import com.Transpo.transpo.repository.RouteRepository;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;

    public ScheduleMapper(BusRepository busRepository, RouteRepository routeRepository) {
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
    }

    public ScheduleDTO toDto(Schedule schedule) {
        if (schedule == null) return null;
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        if (schedule.getBus() != null) {
            dto.setBusId(schedule.getBus().getId());
            dto.setBusNumber(schedule.getBus().getBusNumber());
        }
        if (schedule.getRoute() != null) {
            Route route = schedule.getRoute();
            dto.setRouteId(route.getId());
            dto.setOrigin(route.getOrigin());
            dto.setDestination(route.getDestination());
            // copy stop fields so frontend can show updated non-null stops
            dto.setStop01(route.getStop01());
            dto.setStop02(route.getStop02());
            dto.setStop03(route.getStop03());
            dto.setStop04(route.getStop04());
            dto.setStop05(route.getStop05());
            dto.setStop06(route.getStop06());
            dto.setStop07(route.getStop07());
            dto.setStop08(route.getStop08());
            dto.setStop09(route.getStop09());
            dto.setStop10(route.getStop10());
        }
        dto.setDepartureTime(schedule.getDepartureTime());
        dto.setFare(schedule.getFare());
        dto.setAvailableSeats(schedule.getAvailableSeats());
        return dto;
    }

    public Schedule toEntity(ScheduleDTO dto) {
        if (dto == null) return null;
        Schedule schedule = new Schedule();
        schedule.setId(dto.getId());
        schedule.setDepartureTime(dto.getDepartureTime());
        schedule.setFare(dto.getFare());
        schedule.setAvailableSeats(dto.getAvailableSeats());

    if (dto.getBusId() != null) {
        Bus bus = busRepository.findById(dto.getBusId())
            .orElseThrow(() -> new IllegalArgumentException("Bus not found with id " + dto.getBusId()));
        schedule.setBus(bus);
    }

    if (dto.getRouteId() != null) {
        Route route = routeRepository.findById(dto.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found with id " + dto.getRouteId()));
        schedule.setRoute(route);
    }

    return schedule;
    }
}
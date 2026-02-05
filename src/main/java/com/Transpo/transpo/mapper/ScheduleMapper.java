package com.Transpo.transpo.mapper;

import com.Transpo.transpo.dto.ScheduleDTO;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.BusRepository;
import com.Transpo.transpo.repository.RouteRepository;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {

    private static BusRepository busRepository;
    private static RouteRepository routeRepository;

    // Constructor injection for static fields
    public ScheduleMapper(BusRepository busRepository, RouteRepository routeRepository) {
        ScheduleMapper.busRepository = busRepository;
        ScheduleMapper.routeRepository = routeRepository;
    }

    public static ScheduleDTO toDto(Schedule s) {
        if (s == null) return null;
        
        ScheduleDTO d = new ScheduleDTO();
        d.setId(s.getId());
        
        // Set bus details
        if (s.getBus() != null) {
            d.setBusId(s.getBus().getId());
            d.setBusNumber(s.getBus().getBusNumber());
        } else if (s.getBus() == null && busRepository != null) {
            // Try to load bus if not already loaded
            s.setBus(busRepository.findById(s.getBusId()).orElse(null));
            if (s.getBus() != null) {
                d.setBusId(s.getBus().getId());
                d.setBusNumber(s.getBus().getBusNumber());
            }
        }
        
        // Set route details
        if (s.getRoute() != null) {
            d.setRouteId(s.getRoute().getId());
            d.setOrigin(s.getRoute().getOrigin());
            d.setDestination(s.getRoute().getDestination());
        } else if (s.getRoute() == null && routeRepository != null) {
            // Try to load route if not already loaded
            s.setRoute(routeRepository.findById(s.getRouteId()).orElse(null));
            if (s.getRoute() != null) {
                d.setRouteId(s.getRoute().getId());
                d.setOrigin(s.getRoute().getOrigin());
                d.setDestination(s.getRoute().getDestination());
            }
        }
        
        d.setDepartureTime(s.getDepartureTime());
        d.setFare(s.getFare());
        d.setAvailableSeats(s.getAvailableSeats());
        return d;
    }
    
    // Helper method to create a schedule from DTO
    public static Schedule toEntity(ScheduleDTO dto) {
        if (dto == null) return null;
        
        Schedule schedule = new Schedule();
        schedule.setId(dto.getId());
        
        // Set bus (just set the ID, service will load full entity)
        if (dto.getBusId() != null) {
            // Create a minimal bus with just ID
            com.Transpo.transpo.model.Bus bus = new com.Transpo.transpo.model.Bus();
            bus.setId(dto.getBusId());
            schedule.setBus(bus);
        }
        
        // Set route (just set the ID, service will load full entity)
        if (dto.getRouteId() != null) {
            com.Transpo.transpo.model.Route route = new com.Transpo.transpo.model.Route();
            route.setId(dto.getRouteId());
            schedule.setRoute(route);
        }
        
        schedule.setDepartureTime(dto.getDepartureTime());
        schedule.setFare(dto.getFare());
        schedule.setAvailableSeats(dto.getAvailableSeats());
        
        return schedule;
    }
}
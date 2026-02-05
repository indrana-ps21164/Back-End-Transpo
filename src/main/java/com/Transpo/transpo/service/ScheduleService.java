package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.BusRepository;
import com.Transpo.transpo.repository.ScheduleRepository;
import com.Transpo.transpo.repository.ReservationRepository;
import com.Transpo.transpo.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepo;
    private final ReservationRepository reservationRepo;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    

    public ScheduleService(ScheduleRepository scheduleRepo, 
                          ReservationRepository reservationRepo,
                          BusRepository busRepository,
                          RouteRepository routeRepository) {
        this.scheduleRepo = scheduleRepo;
        this.reservationRepo = reservationRepo;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
    }

    @Transactional
    public Schedule create(Schedule s) {
        // Load full Bus entity
        if (s.getBus() != null && s.getBus().getId() != null) {
            Bus bus = busRepository.findById(s.getBus().getId())
                .orElseThrow(() -> new NotFoundException("Bus not found with id: " + s.getBus().getId()));
            s.setBus(bus);
        }
        
        // Load full Route entity
        if (s.getRoute() != null && s.getRoute().getId() != null) {
            Route route = routeRepository.findById(s.getRoute().getId())
                .orElseThrow(() -> new NotFoundException("Route not found with id: " + s.getRoute().getId()));
            s.setRoute(route);
        }

        // Set available seats if not set
        if ((s.getAvailableSeats() == 0 || s.getAvailableSeats() < 0) && s.getBus() != null) {
            s.setAvailableSeats(s.getBus().getTotalSeats());
        }
        
        return scheduleRepo.save(s);
    }

    @Transactional(readOnly = true)
    public List<Schedule> list() {
        List<Schedule> schedules = scheduleRepo.findAll();
        // Ensure related entities are loaded
        schedules.forEach(s -> {
            if (s.getBus() != null) {
                s.getBus().getBusNumber(); // Trigger lazy loading
            }
            if (s.getRoute() != null) {
                s.getRoute().getOrigin(); // Trigger lazy loading
            }
        });
        return schedules;
    }

    @Transactional(readOnly = true)
    public Schedule get(Long id) {
        Schedule schedule = scheduleRepo.findById(id).orElse(null);
        if (schedule != null) {
            // Force loading of related entities
            if (schedule.getBus() != null) {
                schedule.getBus().getBusNumber();
            }
            if (schedule.getRoute() != null) {
                schedule.getRoute().getOrigin();
            }
        }
        return schedule;
    }

    /**
     * Return reserved seat numbers for schedule and available seats count.
     */
    @Transactional(readOnly = true)
    public SeatInfo getSeatInfo(Long scheduleId) {
        List<Integer> reserved = reservationRepo.findByScheduleId(scheduleId)
                .stream()
                .map(r -> r.getSeatNumber())
                .collect(Collectors.toList());
        Schedule s = get(scheduleId);  // Use our get method which loads relations
        int available = s == null ? 0 : s.getAvailableSeats();
        int totalSeats = s == null || s.getBus() == null ? 0 : s.getBus().getTotalSeats();
        
        return new SeatInfo(reserved, available, totalSeats);
    }

    public static class SeatInfo {
        private final List<Integer> reservedSeats;
        private final int availableSeats;
        private final int totalSeats;
        
        public SeatInfo(List<Integer> reservedSeats, int availableSeats, int totalSeats) {
            this.reservedSeats = reservedSeats;
            this.availableSeats = availableSeats;
            this.totalSeats = totalSeats;
        }
        
        public List<Integer> getReservedSeats() { return reservedSeats; }
        public int getAvailableSeats() { return availableSeats; }
        public int getTotalSeats() { return totalSeats; }
    }
}
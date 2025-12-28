package com.Transpo.transpo.service;

import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.ScheduleRepository;
import com.Transpo.transpo.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepo;
    private final ReservationRepository reservationRepo;
    private final ReservationRuleService ruleService;

    public ScheduleService(ScheduleRepository scheduleRepo, 
                          ReservationRepository reservationRepo,
                          ReservationRuleService ruleService) {
        this.scheduleRepo = scheduleRepo;
        this.reservationRepo = reservationRepo;
        this.ruleService = ruleService;
    }

    public Schedule create(Schedule s) {
        if ((s.getAvailableSeats() == 0 || s.getAvailableSeats() < 0) && s.getBus() != null) {
            s.setAvailableSeats(s.getBus().getTotalSeats());
        }
        return scheduleRepo.save(s);
    }

    public List<Schedule> list() {
        return scheduleRepo.findAll();
    }

    public Schedule get(Long id) {
        return scheduleRepo.findById(id).orElse(null);
    }

    /**
     * Return reserved seat numbers for schedule and available seats count.
     */
    public SeatInfo getSeatInfo(Long scheduleId) {
        List<Integer> reserved = reservationRepo.findByScheduleId(scheduleId)
                .stream()
                .map(r -> r.getSeatNumber())
                .collect(Collectors.toList());
        Schedule s = scheduleRepo.findById(scheduleId).orElse(null);
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
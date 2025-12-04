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

    public ScheduleService(ScheduleRepository scheduleRepo, ReservationRepository reservationRepo) {
        this.scheduleRepo = scheduleRepo;
        this.reservationRepo = reservationRepo;
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
                .stream().map(r -> r.getSeatNumber()).collect(Collectors.toList());
        Schedule s = scheduleRepo.findById(scheduleId).orElse(null);
        int available = s == null ? 0 : s.getAvailableSeats();
        return new SeatInfo(reserved, available);
    }

    public static class SeatInfo {
        private final List<Integer> reservedSeats;
        private final int availableSeats;
        public SeatInfo(List<Integer> reservedSeats, int availableSeats) {
            this.reservedSeats = reservedSeats;
            this.availableSeats = availableSeats;
        }
        public List<Integer> getReservedSeats() { return reservedSeats; }
        public int getAvailableSeats() { return availableSeats; }
    }
}

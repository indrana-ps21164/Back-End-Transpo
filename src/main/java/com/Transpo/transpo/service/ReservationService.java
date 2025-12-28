package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.ConflictException;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.ReservationRepository;
import com.Transpo.transpo.repository.ScheduleRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ScheduleRepository scheduleRepo;
    private final ReservationRuleService ruleService;

    public ReservationService(ReservationRepository reservationRepo, 
                             ScheduleRepository scheduleRepo,
                             ReservationRuleService ruleService) {
        this.reservationRepo = reservationRepo;
        this.scheduleRepo = scheduleRepo;
        this.ruleService = ruleService;
    }

    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    @Transactional
    public Reservation bookSeat(Long scheduleId, String passengerName, 
                               String passengerEmail, int seatNumber) {
        
        // Lock schedule row for update
        Schedule schedule = scheduleRepo.findScheduleById(scheduleId);
        if (schedule == null) {
            throw new NotFoundException("Schedule not found: " + scheduleId);
        }

        if (schedule.getBus() == null) {
            throw new BadRequestException("Schedule does not have a bus assigned");
        }

        int maxSeat = schedule.getBus().getTotalSeats();
        if (seatNumber < 1 || seatNumber > maxSeat) {
            throw new BadRequestException("Seat number must be between 1 and " + maxSeat);
        }

        if (schedule.getAvailableSeats() <= 0) {
            throw new ConflictException("No seats available");
        }

        // Check duplicate seat booking
        List<Reservation> existing = reservationRepo.findByScheduleId(scheduleId);
        for (Reservation r : existing) {
            if (r.getSeatNumber() == seatNumber) {
                throw new ConflictException("Seat " + seatNumber + " already taken for this schedule");
            }
        }

        // Apply business rules based on user role
        ruleService.validateReservationRules(getCurrentUsername(), schedule, true);

        // Decrease availability
        schedule.setAvailableSeats(schedule.getAvailableSeats() - 1);
        scheduleRepo.save(schedule);

        // Create reservation
        Reservation res = new Reservation();
        res.setSchedule(schedule);
        res.setPassengerName(passengerName);
        res.setPassengerEmail(passengerEmail);
        res.setSeatNumber(seatNumber);

        return reservationRepo.save(res);
    }

    public List<Reservation> getByEmail(String email) {
        return reservationRepo.findByPassengerEmail(email);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
        
        Schedule schedule = r.getSchedule();
        if (schedule == null) {
            throw new BadRequestException("Reservation has no associated schedule");
        }

        // Apply business rules for cancellation
        ruleService.validateReservationRules(getCurrentUsername(), schedule, false);

        // Increase availability
        schedule.setAvailableSeats(schedule.getAvailableSeats() + 1);
        scheduleRepo.save(schedule);
        
        reservationRepo.delete(r);
    }

    /**
     * Get seat info with passenger allocation information
     */
    public SeatInfoWithAllocation getSeatInfoWithAllocation(Long scheduleId) {
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));
        
        List<Integer> reserved = reservationRepo.findByScheduleId(scheduleId)
                .stream()
                .map(Reservation::getSeatNumber)
                .toList();
        
        int totalSeats = schedule.getBus().getTotalSeats();
        int availableSeats = schedule.getAvailableSeats();
        int passengerAllocatedSeats = Math.max(1, (int) Math.ceil(totalSeats * 0.2));
        int remainingPassengerSeats = ruleService.getRemainingPassengerSeats(schedule);
        
        return new SeatInfoWithAllocation(
            reserved, 
            availableSeats, 
            totalSeats,
            passengerAllocatedSeats,
            remainingPassengerSeats
        );
    }

    public static class SeatInfoWithAllocation {
        private final List<Integer> reservedSeats;
        private final int availableSeats;
        private final int totalSeats;
        private final int passengerAllocatedSeats;
        private final int remainingPassengerSeats;

        public SeatInfoWithAllocation(List<Integer> reservedSeats, int availableSeats, 
                                     int totalSeats, int passengerAllocatedSeats, 
                                     int remainingPassengerSeats) {
            this.reservedSeats = reservedSeats;
            this.availableSeats = availableSeats;
            this.totalSeats = totalSeats;
            this.passengerAllocatedSeats = passengerAllocatedSeats;
            this.remainingPassengerSeats = remainingPassengerSeats;
        }

        public List<Integer> getReservedSeats() { return reservedSeats; }
        public int getAvailableSeats() { return availableSeats; }
        public int getTotalSeats() { return totalSeats; }
        public int getPassengerAllocatedSeats() { return passengerAllocatedSeats; }
        public int getRemainingPassengerSeats() { return remainingPassengerSeats; }
    }
}
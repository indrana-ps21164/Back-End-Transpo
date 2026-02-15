package com.Transpo.transpo.service;

import com.Transpo.transpo.Role;
import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ReservationRuleService {

    private final UserRepository userRepository;

    public ReservationRuleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validate if user can book/cancel reservation based on role and time
     * @param username Username of the current user
     * @param schedule The schedule for which reservation is being made
     * @param isBooking true for booking, false for cancellation
     */
    public void validateReservationRules(String username, Schedule schedule, boolean isBooking) {
    // Rules disabled: any role can book/cancel at any time.
    return;
    }

    /**
     * Validate 30-minute rule for PASSENGER
     */
    private void validatePassengerTimeRestriction(Schedule schedule, boolean isBooking) { /* no-op */ }

    /**
     * Validate 20% seat allocation rule for PASSENGER
     */
    private void validatePassengerSeatAllocation(Schedule schedule) { /* no-op */ }

    /**
     * Calculate remaining seats available for PASSENGER booking
     */
    public int getRemainingPassengerSeats(Schedule schedule) {
        int totalSeats = schedule.getBus().getTotalSeats();
        int availableSeats = schedule.getAvailableSeats();
        int passengerAllocatedSeats = Math.max(1, (int) Math.ceil(totalSeats * 0.2));
        int seatsTakenByPassengers = totalSeats - availableSeats;
        
        return Math.max(0, passengerAllocatedSeats - seatsTakenByPassengers);
    }
}
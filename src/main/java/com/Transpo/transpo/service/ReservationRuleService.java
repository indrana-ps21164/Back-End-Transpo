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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // If user is CONDUCTOR or ADMIN, no restrictions
        if (user.getRole() == Role.CONDUCTOR || user.getRole() == Role.ADMIN) {
            return;
        }

        // For PASSENGER, apply the 30-minute rule
        if (user.getRole() == Role.PASSENGER) {
            validatePassengerTimeRestriction(schedule, isBooking);
            
            // For booking only, also validate the 20% rule
            if (isBooking) {
                validatePassengerSeatAllocation(schedule);
            }
        }
    }

    /**
     * Validate 30-minute rule for PASSENGER
     */
    private void validatePassengerTimeRestriction(Schedule schedule, boolean isBooking) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = schedule.getDepartureTime();
        
        // Calculate time difference
        Duration duration = Duration.between(now, departureTime);
        long minutesUntilDeparture = duration.toMinutes();
        
        if (minutesUntilDeparture < 30) {
            String action = isBooking ? "book" : "cancel";
            throw new BadRequestException(
                String.format("PASSENGER can only %s reservations at least 30 minutes before departure. " +
                            "Current time until departure: %d minutes", 
                            action, minutesUntilDeparture)
            );
        }
    }

    /**
     * Validate 20% seat allocation rule for PASSENGER
     */
    private void validatePassengerSeatAllocation(Schedule schedule) {
        int totalSeats = schedule.getBus().getTotalSeats();
        int availableSeats = schedule.getAvailableSeats();
        
        // Calculate 20% of total seats (minimum 1 seat)
        int passengerAllocatedSeats = Math.max(1, (int) Math.ceil(totalSeats * 0.2));
        
        // Calculate seats already booked for this schedule (excluding the one being booked)
        int seatsTakenByPassengers = totalSeats - availableSeats;
        
        // If all passenger-allocated seats are already taken
        if (seatsTakenByPassengers >= passengerAllocatedSeats) {
            throw new BadRequestException(
                String.format("PASSENGER booking limit reached. Only %d out of %d seats are allocated for passengers. " +
                            "Please contact conductor for assistance.",
                            passengerAllocatedSeats, totalSeats)
            );
        }
    }

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
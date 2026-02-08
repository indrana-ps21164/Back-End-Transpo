package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.ConflictException;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.dto.ReservationDTO;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.ReservationRepository;
import com.Transpo.transpo.repository.ScheduleRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Transpo.transpo.model.BusStop;
import com.Transpo.transpo.repository.BusStopRepository;
import com.Transpo.transpo.repository.DriverAssignmentRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ScheduleRepository scheduleRepo;
    private final ReservationRuleService ruleService;
    private final BusStopRepository busStopRepo;
    private final DriverAssignmentRepository driverAssignmentRepo;

    public ReservationService(ReservationRepository reservationRepo, 
                             ScheduleRepository scheduleRepo,
                             ReservationRuleService ruleService,
                             BusStopRepository busStopRepo,
                             DriverAssignmentRepository driverAssignmentRepo) {
        this.reservationRepo = reservationRepo;
        this.scheduleRepo = scheduleRepo;
        this.ruleService = ruleService;
        this.busStopRepo = busStopRepo;
        this.driverAssignmentRepo = driverAssignmentRepo;
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
                               String passengerEmail, int seatNumber
                               , Long pickupStopId, Long dropStopId) {
        
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

         // Validate pickup and drop stops
        BusStop pickupStop = null;
        BusStop dropStop = null;
        
        if (pickupStopId != null) {
            pickupStop = busStopRepo.findById(pickupStopId)
                    .orElseThrow(() -> new NotFoundException("Pickup stop not found: " + pickupStopId));
            // Verify pickup stop belongs to the route of this schedule
            if (!pickupStop.getRoute().getId().equals(schedule.getRoute().getId())) {
                throw new BadRequestException("Pickup stop does not belong to this route");
            }
        }
        
        if (dropStopId != null) {
            dropStop = busStopRepo.findById(dropStopId)
                    .orElseThrow(() -> new NotFoundException("Drop stop not found: " + dropStopId));
            // Verify drop stop belongs to the route of this schedule
            if (!dropStop.getRoute().getId().equals(schedule.getRoute().getId())) {
                throw new BadRequestException("Drop stop does not belong to this route");
            }
        }
        
        // Verify pickup comes before drop in the route sequence
        if (pickupStop != null && dropStop != null) {
            if (pickupStop.getSequence() >= dropStop.getSequence()) {
                throw new BadRequestException("Pickup stop must come before drop stop in the route");
            }
        }

        // Decrease availability
        schedule.setAvailableSeats(schedule.getAvailableSeats() - 1);
        scheduleRepo.save(schedule);

        // Create reservation
        Reservation res = new Reservation();
        res.setSchedule(schedule);
        res.setPassengerName(passengerName);
        res.setPassengerEmail(passengerEmail);
        res.setSeatNumber(seatNumber);
        res.setPickupStop(pickupStop);
        res.setDropStop(dropStop);

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

    @Transactional
    public Reservation updateReservation(Long reservationId, ReservationDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Reservation update payload is required");
        }

        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        if (dto.getScheduleId() == null) {
            throw new BadRequestException("scheduleId is required");
        }

        Schedule newSchedule = scheduleRepo.findScheduleById(dto.getScheduleId());
        if (newSchedule == null) {
            throw new NotFoundException("Schedule not found: " + dto.getScheduleId());
        }

        if (newSchedule.getBus() == null) {
            throw new BadRequestException("Schedule does not have a bus assigned");
        }

        int seatNumber = dto.getSeatNumber();
        if (seatNumber < 1 || seatNumber > newSchedule.getBus().getTotalSeats()) {
            throw new BadRequestException("Seat number must be between 1 and " + newSchedule.getBus().getTotalSeats());
        }

        if (dto.getPassengerName() == null || dto.getPassengerName().isBlank()) {
            throw new BadRequestException("passengerName is required");
        }
        if (dto.getPassengerEmail() == null || dto.getPassengerEmail().isBlank()) {
            throw new BadRequestException("passengerEmail is required");
        }

        BusStop pickupStop = null;
        BusStop dropStop = null;

        if (dto.getPickupStopId() != null) {
            pickupStop = busStopRepo.findById(dto.getPickupStopId())
                    .orElseThrow(() -> new NotFoundException("Pickup stop not found: " + dto.getPickupStopId()));
            if (!pickupStop.getRoute().getId().equals(newSchedule.getRoute().getId())) {
                throw new BadRequestException("Pickup stop does not belong to this route");
            }
        }

        if (dto.getDropStopId() != null) {
            dropStop = busStopRepo.findById(dto.getDropStopId())
                    .orElseThrow(() -> new NotFoundException("Drop stop not found: " + dto.getDropStopId()));
            if (!dropStop.getRoute().getId().equals(newSchedule.getRoute().getId())) {
                throw new BadRequestException("Drop stop does not belong to this route");
            }
        }

        if (pickupStop != null && dropStop != null) {
            if (pickupStop.getSequence() >= dropStop.getSequence()) {
                throw new BadRequestException("Pickup stop must come before drop stop in the route");
            }
        }

        Schedule oldSchedule = reservation.getSchedule();
        if (oldSchedule == null) {
            throw new BadRequestException("Reservation has no associated schedule");
        }

        boolean scheduleChanged = !oldSchedule.getId().equals(newSchedule.getId());

        if (scheduleChanged && newSchedule.getAvailableSeats() <= 0) {
            throw new ConflictException("No seats available");
        }

        List<Reservation> existing = reservationRepo.findByScheduleId(newSchedule.getId());
        for (Reservation r : existing) {
            if (!r.getId().equals(reservationId) && r.getSeatNumber() == seatNumber) {
                throw new ConflictException("Seat " + seatNumber + " already taken for this schedule");
            }
        }

        if (scheduleChanged) {
            oldSchedule.setAvailableSeats(oldSchedule.getAvailableSeats() + 1);
            newSchedule.setAvailableSeats(newSchedule.getAvailableSeats() - 1);
            scheduleRepo.save(oldSchedule);
            scheduleRepo.save(newSchedule);
        }

        reservation.setSchedule(newSchedule);
        reservation.setPassengerName(dto.getPassengerName());
        reservation.setPassengerEmail(dto.getPassengerEmail());
        reservation.setSeatNumber(seatNumber);
        reservation.setPickupStop(pickupStop);
        reservation.setDropStop(dropStop);

        return reservationRepo.save(reservation);
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
    /**
     * Get reservations by driver (for their assigned bus)
     */
    public List<Reservation> getReservationsForDriver(String username) {
        // Find driver assignment
        var assignment = driverAssignmentRepo.findByDriverUsername(username)
                .orElseThrow(() -> new NotFoundException("Driver assignment not found"));
        
        // Get all schedules for driver's bus
        List<Schedule> schedules = scheduleRepo.findAll().stream()
                .filter(s -> s.getBus().getId().equals(assignment.getBus().getId()))
                .collect(Collectors.toList());
        
        // Get all reservations for these schedules
        return schedules.stream()
                .flatMap(s -> reservationRepo.findByScheduleId(s.getId()).stream())
                .collect(Collectors.toList());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }
}

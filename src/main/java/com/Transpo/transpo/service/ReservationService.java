package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.ConflictException;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.dto.ReservationDTO;
import com.Transpo.transpo.dto.SeatAvailabilityDTO;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.ReservationRepository;
import com.Transpo.transpo.repository.ScheduleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Transpo.transpo.model.BusStop;
import com.Transpo.transpo.repository.BusStopRepository;
import com.Transpo.transpo.repository.DriverAssignmentRepository;
import com.Transpo.transpo.repository.SeatStateRepository;
import com.Transpo.transpo.repository.ConductorAssignmentRepository;
import com.Transpo.transpo.model.SeatState;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ScheduleRepository scheduleRepo;
    private final ReservationRuleService ruleService;
    private final BusStopRepository busStopRepo;
    private final DriverAssignmentRepository driverAssignmentRepo;
    private final SeatStateRepository seatStateRepo;
    private final ConductorAssignmentRepository conductorAssignmentRepo;

    public ReservationService(ReservationRepository reservationRepo, 
                             ScheduleRepository scheduleRepo,
                             ReservationRuleService ruleService,
                             BusStopRepository busStopRepo,
                             DriverAssignmentRepository driverAssignmentRepo,
                             SeatStateRepository seatStateRepo,
                             ConductorAssignmentRepository conductorAssignmentRepo) {
        this.reservationRepo = reservationRepo;
        this.scheduleRepo = scheduleRepo;
        this.ruleService = ruleService;
        this.busStopRepo = busStopRepo;
        this.driverAssignmentRepo = driverAssignmentRepo;
        this.seatStateRepo = seatStateRepo;
        this.conductorAssignmentRepo = conductorAssignmentRepo;
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
        if (maxSeat < 1) {
            throw new BadRequestException("Bus capacity is invalid for this schedule");
        }
        if (seatNumber < 1 || seatNumber > maxSeat) {
            throw new BadRequestException("Seat number must be between 1 and " + maxSeat);
        }

        if (schedule.getAvailableSeats() <= 0) {
            throw new ConflictException("No seats available");
        }

        // Check duplicate seat booking and duplicate booking by the same user
        List<Reservation> existing = reservationRepo.findByScheduleId(scheduleId);
        String currentUser = getCurrentUsername();
        for (Reservation r : existing) {
            if (r.getSeatNumber() == seatNumber) {
                throw new ConflictException("Seat " + seatNumber + " already taken for this schedule");
            }
            if (r.getUsername() != null && r.getUsername().equals(currentUser)) {
                throw new ConflictException("You have already booked a seat on this schedule");
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
        // set username from logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            res.setUsername(auth.getName());
        }

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

    /**
     * Get reservations by conductor (for their assigned bus)
     */
    public List<Reservation> getReservationsForConductor(String username) {
    var assignment = conductorAssignmentRepo.findAll().stream()
        .filter(a -> a.getConductor() != null && username.equals(a.getConductor().getUsername()))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Conductor assignment not found"));
    List<Schedule> schedules = scheduleRepo.findAll().stream()
        .filter(s -> s.getBus() != null && s.getBus().getId().equals(assignment.getBus().getId()))
        .collect(Collectors.toList());
    return schedules.stream()
        .flatMap(s -> reservationRepo.findByScheduleId(s.getId()).stream())
        .collect(Collectors.toList());
    }

    /**
     * Get reservations for the currently logged-in user
     */
    public List<Reservation> getReservationsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }
        String username = auth.getName();
        return reservationRepo.findByUsername(username);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }

    /**
     * Build seat availability grid by bus with role-based filtering.
     */
    public SeatAvailabilityDTO getSeatAvailability(Long busId, String busNumber, Long scheduleId, String username) {
        if (busId == null && (busNumber == null || busNumber.isBlank())) {
            throw new BadRequestException("busId or busNumber is required");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = username != null ? username : (auth != null ? auth.getName() : null);
        if (user == null) throw new BadRequestException("Authenticated user required");

        // Determine schedule: use provided or pick the first upcoming schedule for this bus
        Schedule schedule;
        if (scheduleId != null) {
            schedule = scheduleRepo.findById(scheduleId)
                    .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));
            if (busId != null && (schedule.getBus() == null || !schedule.getBus().getId().equals(busId))) {
                throw new BadRequestException("Schedule does not belong to bus");
            }
        } else {
            schedule = scheduleRepo.findAll().stream()
                    .filter(s -> {
                        if (s.getBus() == null) return false;
                        if (busId != null) return s.getBus().getId().equals(busId);
                        return s.getBus().getBusNumber() != null && s.getBus().getBusNumber().equalsIgnoreCase(busNumber);
                    })
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("No schedule for bus: " + (busId != null ? busId : busNumber)));
        }

        int totalSeats = schedule.getBus().getTotalSeats();
        var reservations = reservationRepo.findByScheduleId(schedule.getId());

        // Role-based filtering
        var seats = new java.util.ArrayList<SeatAvailabilityDTO.Seat>(totalSeats);

        // Determine role using SecurityContext authorities
    boolean isAdmin = false;
    boolean isConductor = false;
    boolean isDriver = false;
    java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities =
        auth != null ? auth.getAuthorities() : java.util.List.of();
    for (org.springframework.security.core.GrantedAuthority a : authorities) {
            String role = a.getAuthority();
            if (role != null) {
                if (role.contains("ADMIN")) isAdmin = true;
                if (role.contains("CONDUCTOR")) isConductor = true;
                if (role.contains("DRIVER")) isDriver = true;
                if (role.contains("PASSENGER")) {
                    // nothing special to mark
                }
            }
        }

    // If driver, validate assignment to this bus. Allow validation via busId, busNumber, or schedule's bus.
        if (isDriver) {
            var assignmentOpt = driverAssignmentRepo.findByDriverUsername(user);
            var assignment = assignmentOpt.orElseThrow(() -> new NotFoundException("Driver assignment not found"));
            Long assignedBusId = assignment.getBus() != null ? assignment.getBus().getId() : null;
            String assignedBusNumber = assignment.getBus() != null ? assignment.getBus().getBusNumber() : null;

            boolean matchesById = (busId != null && assignedBusId != null && assignedBusId.equals(busId));
            boolean matchesByNumber = (busNumber != null && !busNumber.isBlank() && assignedBusNumber != null && assignedBusNumber.equalsIgnoreCase(busNumber));
            boolean matchesBySchedule = (assignedBusId != null && schedule.getBus() != null && assignedBusId.equals(schedule.getBus().getId()));

            if (!(matchesById || matchesByNumber || matchesBySchedule)) {
                throw new BadRequestException("Driver not assigned to this bus");
            }
        }

        // If conductor, validate assignment similarly
        if (isConductor) {
            // Validate that the accessed bus matches the schedule's bus and provided filters
            boolean matchesById = (busId != null && schedule.getBus() != null && schedule.getBus().getId().equals(busId));
            boolean matchesByNumber = (busNumber != null && !busNumber.isBlank() && schedule.getBus() != null && schedule.getBus().getBusNumber() != null && schedule.getBus().getBusNumber().equalsIgnoreCase(busNumber));
            boolean matchesBySchedule = (schedule.getBus() != null);
            if (!(matchesById || matchesByNumber || matchesBySchedule)) {
                throw new BadRequestException("Conductor not assigned to this bus");
            }
        }

        // Build seat statuses
        java.util.Map<Integer, Reservation> bySeat = new java.util.HashMap<>();
        for (Reservation r : reservations) {
            bySeat.put(r.getSeatNumber(), r);
        }
        for (int i = 1; i <= totalSeats; i++) {
            SeatAvailabilityDTO.Seat seat = new SeatAvailabilityDTO.Seat();
        seat.seatNumber = i;
            Reservation match = bySeat.get(i);
            if (match == null) {
                seat.status = "AVAILABLE";
            } else {
                seat.status = match.isPaid() ? "PAID" : "RESERVED";
                // Expose passenger name only for admin/conductor
                if (isAdmin || isConductor) {
                    seat.passengerName = match.getPassengerName();
                } else if (user.equals(match.getUsername())) {
                    // Passenger sees only own seat name
                    seat.passengerName = match.getPassengerName();
                } else {
                    seat.passengerName = null;
                }
            }
            seats.add(seat);
        }

    SeatAvailabilityDTO dto = new SeatAvailabilityDTO();
    dto.setBusId(schedule.getBus().getId());
    dto.setBusNumber(schedule.getBus().getBusNumber());
        dto.setScheduleId(schedule.getId());
        dto.setTotalSeats(totalSeats);
        // If passenger, hide seats that are not their own as AVAILABLE-only view
        if (!isAdmin && !isConductor && !isDriver) {
            String current = user;
            java.util.ArrayList<SeatAvailabilityDTO.Seat> filtered = new java.util.ArrayList<>();
            for (SeatAvailabilityDTO.Seat s : seats) {
                Reservation match = bySeat.get(s.seatNumber);
                if (match != null && !current.equals(match.getUsername())) {
                    // Hide other passenger data; show status only
                    SeatAvailabilityDTO.Seat ns = new SeatAvailabilityDTO.Seat();
                    ns.seatNumber = s.seatNumber;
                    ns.status = match.isPaid() ? "PAID" : "RESERVED";
                    ns.passengerName = null;
                    filtered.add(ns);
                }
                else { filtered.add(s); }
            }
            seats = filtered;
        }
        dto.setSeats(seats);
        return dto;
    }

    /**
     * Return seat details for a given schedule and seat number: reservation info and current state overlay.
     */
    public java.util.Map<String, Object> getSeatDetails(Long scheduleId, int seatNumber) {
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));
        Reservation match = reservationRepo.findByScheduleId(scheduleId).stream()
                .filter(r -> r.getSeatNumber() == seatNumber)
                .findFirst().orElse(null);
        SeatState state = seatStateRepo.findByScheduleAndSeatNumber(schedule, seatNumber).orElse(null);
        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("seatNumber", seatNumber);
        resp.put("scheduleId", scheduleId);
        if (match != null) {
            resp.put("reserved", true);
            resp.put("reservationId", match.getId());
            resp.put("passengerName", match.getPassengerName());
            resp.put("paid", match.isPaid());
        } else {
            resp.put("reserved", false);
        }
        resp.put("state", state != null ? state.getState() : null);
        return resp;
    }

    /**
     * Conductor-only: update a manual seat state overlay.
     */
    @Transactional
    public void updateSeatState(Long scheduleId, int seatNumber, String state) {
        if (state == null || state.isBlank()) throw new BadRequestException("state is required");
        String normalized = state.toUpperCase();
        java.util.Set<String> allowed = java.util.Set.of("AVAILABLE", "RESERVED", "PAID", "DISABLED");
        if (!allowed.contains(normalized)) throw new BadRequestException("Invalid state");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new BadRequestException("Authentication required");
        boolean isConductor = auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return role != null && role.contains("CONDUCTOR");
        });
        if (!isConductor) throw new BadRequestException("Only conductor can update seat state");

        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));
        SeatState seatState = seatStateRepo.findByScheduleAndSeatNumber(schedule, seatNumber)
                .orElseGet(() -> {
                    SeatState s = new SeatState();
                    s.setSchedule(schedule);
                    s.setSeatNumber(seatNumber);
                    return s;
                });
        seatState.setState(normalized);
        seatState.setUpdatedBy(auth.getName());
        seatStateRepo.save(seatState);
    }
}

package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.ReservationDTO;
import com.Transpo.transpo.mapper.ReservationMapper;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.service.ReservationService;
import com.Transpo.transpo.service.ReservationService.SeatInfoWithAllocation;
import com.Transpo.transpo.dto.SeatAvailabilityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @PostMapping("/book")
    public ResponseEntity<ReservationDTO> book(@RequestBody Map<String, Object> req) {
    // Role check performed inside service (Admin full; Conductor restricted to their bus; Passenger self only)
        Long scheduleId = Long.valueOf(String.valueOf(req.get("scheduleId")));
    String name = req.get("passengerName") != null ? String.valueOf(req.get("passengerName")) : null;
        String email = String.valueOf(req.get("passengerEmail"));
        int seatNumber = Integer.parseInt(String.valueOf(req.get("seatNumber")));
    Long pickupStopId = req.get("pickupStopId") != null ? 
        Long.valueOf(String.valueOf(req.get("pickupStopId"))) : null;
    Long dropStopId = req.get("dropStopId") != null ? 
        Long.valueOf(String.valueOf(req.get("dropStopId"))) : null;
    // Optional name-based fields from frontend; backend will resolve if ids not provided
    // Frontend may send pickup/drop names; backend currently uses IDs. If names are passed, ignore for now.

        // If name missing/blank, use authenticated username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if ((name == null || name.isBlank()) && auth != null && auth.isAuthenticated()) {
            name = auth.getName();
        }
        Reservation r = reservationService.bookSeat(scheduleId, name, email, seatNumber, pickupStopId, dropStopId);
        return ResponseEntity.ok(ReservationMapper.toDto(r));
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<ReservationDTO>> byEmail(@RequestParam String email){
        List<ReservationDTO> list = reservationService.getByEmail(email)
                .stream()
                .map(ReservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{scheduleId}/seat-info")
    public ResponseEntity<SeatInfoWithAllocation> getSeatInfoWithAllocation(@PathVariable Long scheduleId) {
        SeatInfoWithAllocation info = reservationService.getSeatInfoWithAllocation(scheduleId);
        return ResponseEntity.ok(info);
    }

    /**
     * Conductor: Get seat details for a schedule + seat number
     */
    @GetMapping("/seat")
    public ResponseEntity<?> getSeatDetails(@RequestParam Long scheduleId, @RequestParam int seatNumber) {
        var details = reservationService.getSeatDetails(scheduleId, seatNumber);
        return ResponseEntity.ok(details);
    }

    /**
     * Conductor-only: Update seat state for a schedule + seat number
     */
    @PutMapping("/seat/state")
    public ResponseEntity<?> updateSeatState(@RequestParam Long scheduleId, @RequestParam int seatNumber, @RequestParam String state) {
        reservationService.updateSeatState(scheduleId, seatNumber, state);
        return ResponseEntity.ok(java.util.Map.of("message", "Seat state updated"));
    }

    /**
     * Seat availability by bus with role-based filtering.
     */
    @GetMapping("/seat-availability")
    public ResponseEntity<SeatAvailabilityDTO> getSeatAvailability(
            @RequestParam(required = false) Long busId,
            @RequestParam(required = false) String busNumber,
            @RequestParam(required = false) Long scheduleId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        SeatAvailabilityDTO dto = reservationService.getSeatAvailability(busId, busNumber, scheduleId, username);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> update(@PathVariable Long id, @RequestBody ReservationDTO dto) {
    // Role check performed inside service
        Reservation updated = reservationService.updateReservation(id, dto);
        return ResponseEntity.ok(ReservationMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> cancel(@PathVariable Long id) {
    // Role check performed inside service
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(Map.of("message", "Reservation cancelled successfully"));
    }

    @GetMapping
    public ResponseEntity<List<ReservationDTO>> list() {
        List<ReservationDTO> list = reservationService.getAllReservations()
                .stream()
                .map(ReservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Current user's reservations
     */
    @GetMapping("/me")
    public ResponseEntity<List<ReservationDTO>> listForMe() {
        List<ReservationDTO> list = reservationService.getReservationsForCurrentUser()
                .stream()
                .map(ReservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Current user's created reservations (by createdBy)
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationDTO>> listMyCreated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        if (username == null) {
            return ResponseEntity.ok(java.util.List.of());
        }
        List<ReservationDTO> list = reservationService.getReservationsCreatedBy(username)
                .stream()
                .map(ReservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Current user's reservation history
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String,Object>>> listHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        if (username == null) return ResponseEntity.ok(java.util.List.of());
        var list = reservationService.getReservationHistoryForUser(username);
        return ResponseEntity.ok(list);
    }

    /**
     * Conductor-specific: list reservations for the bus assigned to the conductor.
     */
    @GetMapping("/conductor")
    public ResponseEntity<List<ReservationDTO>> listForConductor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        List<ReservationDTO> list = reservationService.getReservationsForDriver(username)
                .stream()
                .map(ReservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}

package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.ReservationDTO;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.service.ConductorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conductor")
public class ConductorController {

    private final ConductorService conductorService;

    public ConductorController(ConductorService conductorService) {
        this.conductorService = conductorService;
    }

    @GetMapping("/my-bus")
    public ResponseEntity<Bus> getMyBus() {
        return ResponseEntity.ok(conductorService.getAssignedBus());
    }

    @PutMapping("/my-bus")
    public ResponseEntity<Bus> changeMyBus(@RequestBody Map<String, Long> request) {
        Long busId = request.get("busId");
        return ResponseEntity.ok(conductorService.changeAssignedBus(busId));
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations() {
        List<Reservation> reservations = conductorService.getReservationsForMyBus();
        List<ReservationDTO> dtoList = reservations.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    private ReservationDTO toDto(Reservation r) {
        ReservationDTO dto = new ReservationDTO();
        // ...map fields from Reservation to ReservationDTO as in your existing mapper...
        dto.setId(r.getId());
        dto.setPassengerEmail(r.getPassengerEmail());
        dto.setSeatNumber(r.getSeatNumber());
        dto.setUsername(r.getUsername());
        // set other fields as needed
        return dto;
    }
}

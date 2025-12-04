package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.ReservationDTO;
import com.Transpo.transpo.mapper.ReservationMapper;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.service.ReservationService;
import org.springframework.http.ResponseEntity;
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
        Long scheduleId = Long.valueOf(String.valueOf(req.get("scheduleId")));
        String name = String.valueOf(req.get("passengerName"));
        String email = String.valueOf(req.get("passengerEmail"));
        int seatNumber = Integer.parseInt(String.valueOf(req.get("seatNumber")));
        Reservation r = reservationService.bookSeat(scheduleId, name, email, seatNumber);
        return ResponseEntity.ok(ReservationMapper.toDto(r));
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<ReservationDTO>> byEmail(@RequestParam String email){
        List<ReservationDTO> list = reservationService.getByEmail(email)
                .stream().map(ReservationMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> cancel(@PathVariable Long id){
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(Map.of("message","cancelled"));
    }
}

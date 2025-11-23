package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService =reservationService;
    }
    //Book seat(JSON)
    @PostMapping("/Book")
    public Reservation book(@RequestBody Map<String,Object>req){
        Long scheduleId =Long.valueOf(String.valueOf(req.get("scheduleId")));
        String name = String.valueOf(req.get("passengerName"));
        String email =String.valueOf(req.get("passengerEmail"));
        int seatNumber = Integer.parseInt(String.valueOf(req.get("seatNumber")));
        return reservationService.bookSeat(scheduleId, name, email, seatNumber);
    }
    @GetMapping("/by-email")
    public List<Reservation>byEmail(@RequestParam String email){
        return reservationService.getByEmail(email);
    }

    @DeleteMapping("/{id}")
    public Map<String,String>cancel(@PathVariable Long id){
        reservationService.cancelReservation(id);
        return Map.of("message","cancelled");
    }
}

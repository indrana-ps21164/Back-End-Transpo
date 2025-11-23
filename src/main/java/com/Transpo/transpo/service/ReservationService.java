package com.Transpo.transpo.service;

import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.ReservationRepository;
import com.Transpo.transpo.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ScheduleRepository scheduleRepo;

    public ReservationService(ReservationRepository reservationRepo, ScheduleRepository scheduleRepo) {
        this.reservationRepo = reservationRepo;
        this.scheduleRepo = scheduleRepo;
    }

    @Transactional
    public Reservation bookSeat(Long scheduleId, String passengerName, String passengerEmail,int seatNumber){
        //lock schedule row for safe decrement
        Schedule schedule = scheduleRepo.findScheduleById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("Schedule with ID " + scheduleId + " not found.");
        }

        if(schedule.getAvailableSeats() <= 0) {
            throw new RuntimeException("No available seats for this schedule.");
        }

        //optional: check seat number validity or duplicates
        List<Reservation> existingReservations = reservationRepo.findByScheduleId(scheduleId);
        for (Reservation r : existingReservations){
            if (r.getSeatNumber() == seatNumber){
                throw new RuntimeException("Seat number " + seatNumber + " is already reserved.");
            }
        }

       schedule.setAvailableSeats(schedule.getAvailableSeats() - 1);
        
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
    public void cancelReservation(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId).orElseThrow(() ->
            new RuntimeException("Reservation with ID " + reservationId + " not found.")
        );
        // Optionally: update the schedule's available seats
        Schedule schedule = r.getSchedule();
        schedule.setAvailableSeats(schedule.getAvailableSeats() + 1);
        reservationRepo.delete(r);
    }
}

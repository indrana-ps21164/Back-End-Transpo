package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.ConflictException;
import com.Transpo.transpo.exception.NotFoundException;
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
    public Reservation bookSeat(Long scheduleId, String passengerName, String passengerEmail, int seatNumber) {
        // lock schedule row for update using repository method with @Lock
        Schedule schedule = scheduleRepo.findScheduleById(scheduleId);
        if (schedule == null) throw new NotFoundException("Schedule not found: " + scheduleId);

        if (schedule.getBus() == null)
            throw new BadRequestException("Schedule does not have a bus assigned");

        int maxSeat = schedule.getBus().getTotalSeats();
        if (seatNumber < 1 || seatNumber > maxSeat)
            throw new BadRequestException("Seat number must be between 1 and " + maxSeat);

        if (schedule.getAvailableSeats() <= 0)
            throw new ConflictException("No seats available");

        // prevent duplicate seat booking
        List<Reservation> existing = reservationRepo.findByScheduleId(scheduleId);
        for (Reservation r : existing) {
            if (r.getSeatNumber() == seatNumber) {
                throw new ConflictException("Seat " + seatNumber + " already taken for this schedule");
            }
        }

        // decrease availability
        schedule.setAvailableSeats(schedule.getAvailableSeats() - 1);
        // optionally scheduleRepo.save(schedule); but within @Transactional it's fine

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
        Reservation r = reservationRepo.findById(reservationId).orElseThrow(
                () -> new NotFoundException("Reservation not found: " + reservationId)
        );
        Schedule schedule = r.getSchedule();
        if (schedule != null) {
            schedule.setAvailableSeats(schedule.getAvailableSeats() + 1);
        }
        reservationRepo.delete(r);
    }
}

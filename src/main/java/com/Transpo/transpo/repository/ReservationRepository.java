package com.Transpo.transpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Transpo.transpo.model.Reservation;
import java.util.List;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPassengerEmail(String email);
    List<Reservation> findByScheduleId(Long scheduleId);
}

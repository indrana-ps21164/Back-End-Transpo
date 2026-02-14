package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.SeatState;
import com.Transpo.transpo.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatStateRepository extends JpaRepository<SeatState, Long> {
    Optional<SeatState> findByScheduleAndSeatNumber(Schedule schedule, int seatNumber);
    List<SeatState> findBySchedule(Schedule schedule);
}

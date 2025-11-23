package com.Transpo.transpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Transpo.transpo.model.Schedule;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByRouteOriginAndRouteDestination(String origin, String destination);

    //lock a schedule row for writing during booking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Schedule findScheduleById(Long id);
}

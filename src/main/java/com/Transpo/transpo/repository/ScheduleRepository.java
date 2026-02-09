package com.Transpo.transpo.repository;

import com.Transpo.transpo.dto.ScheduleResponseDTO;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByRouteOriginAndRouteDestination(String origin, String destination);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Schedule s where s.id = :id")
    Schedule findScheduleById(@Param("id") Long id);

    // Custom query to get schedule with all details
    @Query("SELECT new com.Transpo.transpo.dto.ScheduleResponseDTO(" +
           "s.id, b.id, b.busNumber, r.id, r.origin, r.destination, " +
           "s.departureTime, s.fare, s.availableSeats) " +
           "FROM Schedule s " +
           "JOIN s.bus b " +
           "JOIN s.route r " +
           "WHERE s.id = :id")
    Optional<ScheduleResponseDTO> findScheduleDetailsById(@Param("id") Long id);

    @Query("SELECT new com.Transpo.transpo.dto.ScheduleResponseDTO(" +
           "s.id, b.id, b.busNumber, r.id, r.origin, r.destination, " +
           "s.departureTime, s.fare, s.availableSeats) " +
           "FROM Schedule s " +
           "JOIN s.bus b " +
           "JOIN s.route r")
    List<ScheduleResponseDTO> findAllScheduleDetails();

    List<Schedule> findByBus(Bus bus);
}
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
           "s.departureTime, s.fare, s.availableSeats, " +
           "r.stop01, r.stop02, r.stop03, r.stop04, r.stop05, r.stop06, r.stop07, r.stop08, r.stop09, r.stop10) " +
           "FROM Schedule s " +
           "JOIN s.bus b " +
           "JOIN s.route r " +
           "WHERE s.id = :id")
    Optional<ScheduleResponseDTO> findScheduleDetailsById(@Param("id") Long id);

    @Query("SELECT new com.Transpo.transpo.dto.ScheduleResponseDTO(" +
           "s.id, b.id, b.busNumber, r.id, r.origin, r.destination, " +
           "s.departureTime, s.fare, s.availableSeats, " +
           "r.stop01, r.stop02, r.stop03, r.stop04, r.stop05, r.stop06, r.stop07, r.stop08, r.stop09, r.stop10) " +
           "FROM Schedule s " +
           "JOIN s.bus b " +
           "JOIN s.route r")
    List<ScheduleResponseDTO> findAllScheduleDetails();

    List<Schedule> findByBus(Bus bus);

       @Query("SELECT new com.Transpo.transpo.dto.ScheduleResponseDTO(\n" +
                 "           s.id, b.id, b.busNumber, r.id, r.origin, r.destination, \n" +
                 "           s.departureTime, s.fare, s.availableSeats, \n" +
                 "           r.stop01, r.stop02, r.stop03, r.stop04, r.stop05, r.stop06, r.stop07, r.stop08, r.stop09, r.stop10) \n" +
           "FROM Schedule s \n" +
           "JOIN s.bus b \n" +
           "JOIN s.route r \n" +
           "WHERE ( \n" +
           "  LOWER(r.stop01) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop02) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop03) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop04) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop05) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop06) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop07) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop08) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop09) LIKE LOWER(CONCAT('%', :pickup, '%')) OR \n" +
           "  LOWER(r.stop10) LIKE LOWER(CONCAT('%', :pickup, '%')) \n" +
           ") AND ( \n" +
           "  LOWER(r.stop01) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop02) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop03) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop04) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop05) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop06) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop07) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop08) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop09) LIKE LOWER(CONCAT('%', :drop, '%')) OR \n" +
           "  LOWER(r.stop10) LIKE LOWER(CONCAT('%', :drop, '%')) \n" +
           ")")
    List<ScheduleResponseDTO> searchByPickupAndDrop(@Param("pickup") String pickup, @Param("drop") String drop);
}
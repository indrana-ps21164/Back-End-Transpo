package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.DriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Long> {
    
    Optional<DriverAssignment> findByDriverId(Long driverId);
    
    Optional<DriverAssignment> findByDriverUsername(String username);
    
    Optional<DriverAssignment> findByBusId(Long busId);
    
    @Query("SELECT da FROM DriverAssignment da WHERE da.driver.id = :driverId")
    Optional<DriverAssignment> findAssignmentByDriver(@Param("driverId") Long driverId);
}
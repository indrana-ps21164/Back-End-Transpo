package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.ConductorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConductorAssignmentRepository extends JpaRepository<ConductorAssignment, Long> {

    Optional<ConductorAssignment> findByConductorId(Long conductorId);

    Optional<ConductorAssignment> findByBusId(Long busId);
}

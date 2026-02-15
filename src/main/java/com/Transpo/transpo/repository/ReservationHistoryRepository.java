package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.ReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationHistoryRepository extends JpaRepository<ReservationHistory, Long> {
    List<ReservationHistory> findByUsername(String username);
}
package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.TicketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketPriceRepository extends JpaRepository<TicketPrice, Long> {
    @Query("SELECT tp FROM TicketPrice tp WHERE tp.route.id = :routeId")
    List<TicketPrice> findByRouteId(@Param("routeId") Long routeId);

    @Query("SELECT tp FROM TicketPrice tp WHERE tp.route.id = :routeId AND ((tp.fromStopId = :fromId AND tp.toStopId = :toId) OR (tp.fromStopId = :toId AND tp.toStopId = :fromId))")
    Optional<TicketPrice> findSymmetric(@Param("routeId") Long routeId, @Param("fromId") Long fromId, @Param("toId") Long toId);
}

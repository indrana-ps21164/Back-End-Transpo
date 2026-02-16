package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BusStopRepository extends JpaRepository<BusStop, Long> {
    
    List<BusStop> findByRouteIdOrderBySequenceAsc(Long routeId);
    
    @Query("SELECT bs FROM BusStop bs WHERE bs.route.id = :routeId ORDER BY bs.sequence ASC")
    List<BusStop> findStopsByRouteOrdered(@Param("routeId") Long routeId);
    
    @Query("SELECT bs FROM BusStop bs WHERE bs.route.id IN " +
           "(SELECT s.route.id FROM Schedule s WHERE s.bus.id = :busId) " +
           "ORDER BY bs.route.id, bs.sequence ASC")
    List<BusStop> findStopsByBusId(@Param("busId") Long busId);

    @Query("SELECT COUNT(bs) > 0 FROM BusStop bs WHERE bs.id = :stopId AND bs.route.id = :routeId")
    boolean existsByIdAndRouteId(@Param("stopId") Long stopId, @Param("routeId") Long routeId);
}
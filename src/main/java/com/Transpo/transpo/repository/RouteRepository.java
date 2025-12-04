package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByOriginAndDestination(String origin, String destination);
}

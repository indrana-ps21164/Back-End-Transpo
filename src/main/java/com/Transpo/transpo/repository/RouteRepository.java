package com.Transpo.transpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Transpo.transpo.model.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

}

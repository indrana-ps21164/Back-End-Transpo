package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.repository.RouteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RouteRepository routeRepo;

    public RouteController(RouteRepository routeRepo) {
        this.routeRepo = routeRepo;
    }

    @GetMapping
    public List<Route> list() {
        return routeRepo.findAll();
    }

    @PostMapping
    public Route create(@RequestBody Route route) {
        return routeRepo.save(route);
    }

}

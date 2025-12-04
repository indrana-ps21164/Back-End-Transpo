package com.Transpo.transpo.service;

import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository repo;

    public RouteService(RouteRepository repo) {
        this.repo = repo;
    }

    public Route create(Route r) {
        return repo.save(r);
    }

    public List<Route> list() {
        return repo.findAll();
    }

    public Optional<Route> findById(Long id) {
        return repo.findById(id);
    }

    public Route update(Long id, Route updated) {
        return repo.findById(id).map(r -> {
            r.setOrigin(updated.getOrigin());
            r.setDestination(updated.getDestination());
            return repo.save(r);
        }).orElseThrow(() -> new RuntimeException("Route not found"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

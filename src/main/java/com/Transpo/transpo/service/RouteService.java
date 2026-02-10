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
             r.setStop01(updated.getStop01());
        r.setStop02(updated.getStop02());
        r.setStop03(updated.getStop03());
        r.setStop04(updated.getStop04());
        r.setStop05(updated.getStop05());
        r.setStop06(updated.getStop06());
        r.setStop07(updated.getStop07());
        r.setStop08(updated.getStop08());
        r.setStop09(updated.getStop09());
        r.setStop10(updated.getStop10());
            return repo.save(r);
        }).orElseThrow(() -> new RuntimeException("Route not found"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

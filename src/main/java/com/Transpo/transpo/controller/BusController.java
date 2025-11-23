package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.repository.BusRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")

public class BusController {

    private final BusRepository busRepo;

    public BusController(BusRepository busRepo) {
        this.busRepo = busRepo;
    }

    @GetMapping
    public List<Bus> list() {
        return busRepo.findAll();
    }

    @PostMapping
    public Bus create(@RequestBody Bus bus) {
        return busRepo.save(bus);
    }
}

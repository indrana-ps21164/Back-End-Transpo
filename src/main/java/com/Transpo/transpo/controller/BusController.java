package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.BusDTO;
import com.Transpo.transpo.mapper.BusMapper;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.service.BusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;
    public BusController(BusService busService) { this.busService = busService; }

    @PostMapping
    public ResponseEntity<BusDTO> create(@RequestBody BusDTO dto) {
        Bus saved = busService.addBus(BusMapper.toEntity(dto));
        return ResponseEntity.ok(BusMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<BusDTO>> list() {
        List<BusDTO> list = busService.getAllBuses().stream().map(BusMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusDTO> get(@PathVariable Long id) {
        Bus b = busService.getBusById(id);
        if (b == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(BusMapper.toDto(b));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusDTO> update(@PathVariable Long id, @RequestBody BusDTO dto) {
        Bus updated = busService.updateBus(id, BusMapper.toEntity(dto));
        return ResponseEntity.ok(BusMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.ok().build();
    }
}

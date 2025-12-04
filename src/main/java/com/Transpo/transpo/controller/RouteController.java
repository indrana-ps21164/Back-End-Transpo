package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.RouteDTO;
import com.Transpo.transpo.mapper.RouteMapper;
import com.Transpo.transpo.model.Route;
import com.Transpo.transpo.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService service;
    public RouteController(RouteService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<RouteDTO> create(@RequestBody RouteDTO dto) {
        Route r = service.create(RouteMapper.toEntity(dto));
        return ResponseEntity.ok(RouteMapper.toDto(r));
    }

    @GetMapping
    public ResponseEntity<List<RouteDTO>> list() {
        List<RouteDTO> list = service.list().stream().map(RouteMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDTO> get(@PathVariable Long id) {
        return service.findById(id).map(r -> ResponseEntity.ok(RouteMapper.toDto(r))).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteDTO> update(@PathVariable Long id, @RequestBody RouteDTO dto) {
        Route updated = service.update(id, RouteMapper.toEntity(dto));
        return ResponseEntity.ok(RouteMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}

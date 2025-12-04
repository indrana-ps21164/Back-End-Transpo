package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.ScheduleDTO;
import com.Transpo.transpo.mapper.ScheduleMapper;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ScheduleDTO> create(@RequestBody Schedule s) {
        Schedule saved = service.create(s);
        return ResponseEntity.ok(ScheduleMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> list() {
        List<ScheduleDTO> dtos = service.list().stream().map(ScheduleMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> get(@PathVariable Long id) {
        Schedule s = service.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ScheduleMapper.toDto(s));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<?> seats(@PathVariable Long id) {
        var info = service.getSeatInfo(id);
        return ResponseEntity.ok(info);
    }
}

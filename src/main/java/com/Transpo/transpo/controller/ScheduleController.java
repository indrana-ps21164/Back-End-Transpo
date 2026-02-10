package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.ScheduleDTO;
import com.Transpo.transpo.dto.ScheduleResponseDTO;
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
    private final com.Transpo.transpo.repository.ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    public ScheduleController(
            ScheduleService service,
            com.Transpo.transpo.repository.ScheduleRepository scheduleRepository,
            ScheduleMapper scheduleMapper
    ) {
        this.service = service;
        this.scheduleRepository = scheduleRepository;
        this.scheduleMapper = scheduleMapper;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponseDTO> create(@RequestBody ScheduleDTO dto) {
        // Map incoming DTO (with busId, routeId, departureTime, etc.) to entity
        Schedule toSave = scheduleMapper.toEntity(dto);
        Schedule saved = service.create(toSave);

        // Use the custom query to get full details for response
        ScheduleResponseDTO response = scheduleRepository.findScheduleDetailsById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponseDTO>> list() {
        // Use custom query to get all schedules with details
        List<ScheduleResponseDTO> dtos = scheduleRepository.findAllScheduleDetails();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponseDTO> get(@PathVariable Long id) {
        ScheduleResponseDTO dto = scheduleRepository.findScheduleDetailsById(id)
                .orElse(null);

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<?> seats(@PathVariable Long id) {
        var info = service.getSeatInfo(id);
        return ResponseEntity.ok(info);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponseDTO> update(@PathVariable Long id, @RequestBody Schedule s) {
        Schedule updated = service.update(id, s);
        ScheduleResponseDTO dto = scheduleRepository.findScheduleDetailsById(updated.getId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
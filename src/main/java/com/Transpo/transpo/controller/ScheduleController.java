package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.ScheduleDTO;
import com.Transpo.transpo.dto.ScheduleResponseDTO;
import com.Transpo.transpo.mapper.ScheduleMapper;
import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;
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
    public ResponseEntity<List<?>> list(@RequestParam(value = "busNumber", required = false) String busNumber) {
        // If busNumber specified, return only future schedules for that bus as minimal objects
        if (busNumber != null && !busNumber.isBlank()) {
            LocalDateTime now = LocalDateTime.now();
            // Use the DTO-based repository method and filter by bus number and future departure time
            List<ScheduleResponseDTO> all = scheduleRepository.findAllScheduleDetails();
            List<?> minimal = all.stream()
                    .filter(d -> d.getDepartureTime() != null && d.getDepartureTime().isAfter(now) && busNumber.equals(d.getBusNumber()))
                    .map(d -> java.util.Map.of(
                            "id", d.getId(),
                            "departureTime", d.getDepartureTime()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(minimal);
        }
        // Otherwise, return all schedules with details
        List<ScheduleResponseDTO> dtos = scheduleRepository.findAllScheduleDetails();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ScheduleResponseDTO>> search(
            @RequestParam("pickup") String pickup,
            @RequestParam("drop") String drop
    ) {
        List<ScheduleResponseDTO> dtos = scheduleRepository.searchByPickupAndDrop(pickup, drop);
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
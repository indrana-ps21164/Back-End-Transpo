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

    public ScheduleController(ScheduleService service, 
                            com.Transpo.transpo.repository.ScheduleRepository scheduleRepository) {
        this.service = service;
        this.scheduleRepository = scheduleRepository;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponseDTO> create(@RequestBody Schedule s) {
        Schedule saved = service.create(s);
        
        // Use the custom query to get full details
        ScheduleResponseDTO dto = scheduleRepository.findScheduleDetailsById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        return ResponseEntity.ok(dto);
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
}
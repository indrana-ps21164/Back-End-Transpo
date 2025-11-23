package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.Schedule;
import com.Transpo.transpo.repository.ScheduleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepo;

    public ScheduleController(ScheduleRepository scheduleRepo) {
        this.scheduleRepo = scheduleRepo;
    }

    @GetMapping
    public List<Schedule> list() {
        return scheduleRepo.findAll();
    }

    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        if (schedule.getAvailableSeats() == 0 && schedule.getBus() != null) {
            schedule.setAvailableSeats(schedule.getBus().getTotalSeats());
            // Handle case where no seats are available
        }
        return scheduleRepo.save(schedule);
    }

    //search by origin & destination
    @GetMapping("/Search")
    public List<Schedule> search(@RequestParam String origin, @RequestParam String destination) {
        return scheduleRepo.findByRouteOriginAndRouteDestination(origin, destination);
    }

    @GetMapping("/{id}")
    public Schedule get(@PathVariable Long id){
        return scheduleRepo.findById(id).orElse(null);
    }    
}

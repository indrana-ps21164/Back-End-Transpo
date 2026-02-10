package com.Transpo.transpo.service;

import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.*;
import com.Transpo.transpo.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConductorService {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final ConductorAssignmentRepository conductorAssignmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReservationRepository reservationRepository;

    public ConductorService(UserRepository userRepository,
                            BusRepository busRepository,
                            ConductorAssignmentRepository conductorAssignmentRepository,
                            ScheduleRepository scheduleRepository,
                            ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.conductorAssignmentRepository = conductorAssignmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.reservationRepository = reservationRepository;
    }

    private User getCurrentConductor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("No authenticated user");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == null || !"CONDUCTOR".equals(String.valueOf(user.getRole()))) {
            throw new BadRequestException("Current user is not a conductor");
        }
        return user;
    }

    public Bus getAssignedBus() {
        User conductor = getCurrentConductor();
        ConductorAssignment assignment = conductorAssignmentRepository.findByConductorId(conductor.getId())
                .orElseThrow(() -> new NotFoundException("Conductor has no bus assignment"));
        return assignment.getBus();
    }

    public Bus changeAssignedBus(Long busId) {
        if (busId == null) {
            throw new BadRequestException("Bus ID is required");
        }
        User conductor = getCurrentConductor();

        Bus newBus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus not found"));

        // Ensure the bus is not already assigned to another conductor
        conductorAssignmentRepository.findByBusId(busId).ifPresent(existing -> {
            if (!existing.getConductor().getId().equals(conductor.getId())) {
                throw new BadRequestException("Bus is already assigned to another conductor");
            }
        });

        ConductorAssignment assignment = conductorAssignmentRepository.findByConductorId(conductor.getId())
                .orElse(new ConductorAssignment(conductor, newBus));

        assignment.setBus(newBus);
        conductorAssignmentRepository.save(assignment);

        return newBus;
    }

    public List<Reservation> getReservationsForMyBus() {
        User conductor = getCurrentConductor();
        ConductorAssignment assignment = conductorAssignmentRepository.findByConductorId(conductor.getId())
                .orElseThrow(() -> new NotFoundException("Conductor has no bus assignment"));

        List<Schedule> schedules = scheduleRepository.findByBus(assignment.getBus());
        if (schedules.isEmpty()) {
            return List.of();
        }
        List<Long> scheduleIds = schedules.stream().map(Schedule::getId).collect(Collectors.toList());
        return reservationRepository.findByScheduleIdIn(scheduleIds);
    }
}

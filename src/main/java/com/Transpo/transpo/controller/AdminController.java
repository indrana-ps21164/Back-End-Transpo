package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.DriverAssignmentDTO;
import com.Transpo.transpo.exception.BadRequestException;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.model.DriverAssignment;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.model.ConductorAssignment;
import com.Transpo.transpo.repository.BusRepository;
import com.Transpo.transpo.repository.DriverAssignmentRepository;
import com.Transpo.transpo.repository.UserRepository;
import com.Transpo.transpo.repository.ConductorAssignmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final DriverAssignmentRepository driverAssignmentRepository;
    private final ConductorAssignmentRepository conductorAssignmentRepository;

    public AdminController(UserRepository userRepository,
                           BusRepository busRepository,
                           DriverAssignmentRepository driverAssignmentRepository,
                           ConductorAssignmentRepository conductorAssignmentRepository) {
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.driverAssignmentRepository = driverAssignmentRepository;
        this.conductorAssignmentRepository = conductorAssignmentRepository;
    }

    // --- Driver assignment endpoints ---
    @PostMapping("/driver-assignment")
    public ResponseEntity<?> createOrUpdateDriverAssignment(@RequestBody Map<String, Object> payload) {
        return handleDriverAssignment(payload);
    }

    @PutMapping("/driver-assignment")
    public ResponseEntity<?> updateDriverAssignment(@RequestBody Map<String, Object> payload) {
        return handleDriverAssignment(payload);
    }

    private ResponseEntity<?> handleDriverAssignment(Map<String, Object> payload) {
        Long driverId = toLong(payload.get("driverId"));
        String driverUsername = payload.get("username") != null ? String.valueOf(payload.get("username")) : null;
        Long busId = toLong(payload.get("busId"));

        if ((driverId == null && (driverUsername == null || driverUsername.isBlank())) || busId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "driverId/username and busId are required"));
        }

        User driver;
        if (driverId != null) {
            driver = userRepository.findById(driverId).orElse(null);
        } else {
            driver = userRepository.findByUsername(driverUsername).orElse(null);
        }
        if (driver == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Driver not found"));
        }
        // Optional: verify role
        if (driver.getRole() == null || !"DRIVER".equals(String.valueOf(driver.getRole()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not a driver"));
        }

        Bus bus = busRepository.findById(busId).orElse(null);
        if (bus == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Bus not found"));
        }

    // Admin override: clear any existing assignments for this bus and driver, then set new assignment
    driverAssignmentRepository.findByBusId(busId).ifPresent(driverAssignmentRepository::delete);
    driverAssignmentRepository.findByDriverId(driver.getId()).ifPresent(driverAssignmentRepository::delete);

    DriverAssignment assignment = new DriverAssignment(driver, bus);
    assignment.setAssignedAt(LocalDateTime.now());
    driverAssignmentRepository.save(assignment);

        DriverAssignmentDTO dto = new DriverAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setDriverId(driver.getId());
        dto.setDriverUsername(driver.getUsername());
        dto.setBusId(bus.getId());
        dto.setBusNumber(bus.getBusNumber());
        dto.setAssignedAt(assignment.getAssignedAt());
        return ResponseEntity.ok(dto);
    }

    // Remove driver assignment from bus
    @DeleteMapping("/driver-assignment")
    public ResponseEntity<?> removeDriverAssignment(@RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String username) {
        User driver;
        if (userId != null) {
            driver = userRepository.findById(userId).orElse(null);
        } else if (username != null && !username.isBlank()) {
            driver = userRepository.findByUsername(username).orElse(null);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "userId or username required"));
        }
        if (driver == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Driver not found"));
        }
        driverAssignmentRepository.findByDriverId(driver.getId()).ifPresent(da -> {
            driverAssignmentRepository.delete(da);
        });
        return ResponseEntity.ok(Map.of("message", "Driver unassigned from bus"));
    }

    // --- Conductor assignment endpoints ---
    @PostMapping("/conductor-assignment")
    public ResponseEntity<?> createOrUpdateConductorAssignment(@RequestBody Map<String, Object> payload) {
        return handleConductorAssignment(payload);
    }

    @PutMapping("/conductor-assignment")
    public ResponseEntity<?> updateConductorAssignment(@RequestBody Map<String, Object> payload) {
        return handleConductorAssignment(payload);
    }

    private ResponseEntity<?> handleConductorAssignment(Map<String, Object> payload) {
        Long conductorId = toLong(payload.get("userId"));
        String conductorUsername = payload.get("username") != null ? String.valueOf(payload.get("username")) : null;
        Long busId = toLong(payload.get("busId"));

        if ((conductorId == null && (conductorUsername == null || conductorUsername.isBlank())) || busId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId/username and busId are required"));
        }

        User conductor;
        if (conductorId != null) {
            conductor = userRepository.findById(conductorId).orElse(null);
        } else {
            conductor = userRepository.findByUsername(conductorUsername).orElse(null);
        }
        if (conductor == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Conductor not found"));
        }
        if (conductor.getRole() == null || !"CONDUCTOR".equals(String.valueOf(conductor.getRole()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not a conductor"));
        }

        Bus bus = busRepository.findById(busId).orElse(null);
        if (bus == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Bus not found"));
        }

        // Ensure bus isn't already assigned to another conductor
        conductorAssignmentRepository.findByBusId(busId).ifPresent(existing -> {
            if (!existing.getConductor().getId().equals(conductor.getId())) {
                throw new BadRequestException("Bus is already assigned to another conductor");
            }
        });

        ConductorAssignment assignment = conductorAssignmentRepository.findByConductorId(conductor.getId())
                .orElse(new ConductorAssignment(conductor, bus));
        assignment.setBus(bus);
        conductorAssignmentRepository.save(assignment);

        return ResponseEntity.ok(Map.of(
                "message", "Conductor assignment updated",
                "username", conductor.getUsername(),
                "busId", bus.getId(),
                "busNumber", bus.getBusNumber()
        ));
    }

    // Remove conductor assignment from bus
    @DeleteMapping("/conductor-assignment")
    public ResponseEntity<?> removeConductorAssignment(@RequestParam(required = false) Long userId,
                                                       @RequestParam(required = false) String username) {
        User conductor;
        if (userId != null) {
            conductor = userRepository.findById(userId).orElse(null);
        } else if (username != null && !username.isBlank()) {
            conductor = userRepository.findByUsername(username).orElse(null);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "userId or username required"));
        }
        if (conductor == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Conductor not found"));
        }
        conductorAssignmentRepository.findByConductorId(conductor.getId()).ifPresent(ca -> {
            conductorAssignmentRepository.delete(ca);
        });
        return ResponseEntity.ok(Map.of("message", "Conductor unassigned from bus"));
    }

    // List drivers with assignments
    @GetMapping("/drivers")
    public ResponseEntity<List<Map<String, Object>>> listDriversWithAssignments() {
        List<User> drivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "DRIVER".equals(String.valueOf(u.getRole())))
                .collect(Collectors.toList());
        List<Map<String, Object>> result = drivers.stream().map(u -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("username", u.getUsername());
            driverAssignmentRepository.findByDriverId(u.getId()).ifPresent(da -> {
                row.put("assignedBusId", da.getBus().getId());
                row.put("assignedBusNumber", da.getBus().getBusNumber());
                row.put("assignedBusName", da.getBus().getBusName());
            });
            return row;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // --- Conductor listing (assignment change endpoint optional) ---
    @GetMapping("/conductors")
    public ResponseEntity<List<Map<String, Object>>> listConductorsWithAssignments() {
        List<User> conductors = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "CONDUCTOR".equals(String.valueOf(u.getRole())))
                .collect(Collectors.toList());
        List<Map<String, Object>> result = conductors.stream().map(u -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("username", u.getUsername());
            conductorAssignmentRepository.findByConductorId(u.getId()).ifPresent(ca -> {
                row.put("assignedBusId", ca.getBus().getId());
                row.put("assignedBusNumber", ca.getBus().getBusNumber());
                row.put("assignedBusName", ca.getBus().getBusName());
            });
            return row;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Aliases for clarity
    @GetMapping("/assigned-drivers")
    public ResponseEntity<List<Map<String, Object>>> listAssignedDrivers() {
        return listDriversWithAssignments();
    }

    @GetMapping("/assigned-conductors")
    public ResponseEntity<List<Map<String, Object>>> listAssignedConductors() {
        return listConductorsWithAssignments();
    }

    // --- Buses with assigned driver & conductor ---
    @GetMapping("/buses")
    public ResponseEntity<List<Map<String, Object>>> listBusesWithAssignments() {
        List<Bus> buses = busRepository.findAll();
        List<Map<String, Object>> result = buses.stream().map(b -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("id", b.getId());
            row.put("busNumber", b.getBusNumber());
            row.put("busName", b.getBusName());
            // Driver
            driverAssignmentRepository.findByBusId(b.getId()).ifPresent(da -> {
                User drv = da.getDriver();
                row.put("driverId", drv.getId());
                row.put("driverUsername", drv.getUsername());
            });
            // Conductor
            conductorAssignmentRepository.findByBusId(b.getId()).ifPresent(ca -> {
                User con = ca.getConductor();
                row.put("conductorId", con.getId());
                row.put("conductorUsername", con.getUsername());
            });
            return row;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private Long toLong(Object o) {
        try {
            return o == null ? null : Long.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}

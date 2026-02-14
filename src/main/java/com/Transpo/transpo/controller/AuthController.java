package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.RegisterRequest;
import com.Transpo.transpo.Role;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.model.DriverAssignment;
import com.Transpo.transpo.model.ConductorAssignment;
import com.Transpo.transpo.model.Bus;
import com.Transpo.transpo.repository.DriverAssignmentRepository;
import com.Transpo.transpo.repository.ConductorAssignmentRepository;
import com.Transpo.transpo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final DriverAssignmentRepository driverAssignmentRepo;
    private final ConductorAssignmentRepository conductorAssignmentRepo;

    public AuthController(UserRepository userRepo,
                          BCryptPasswordEncoder encoder,
                          AuthenticationManager authManager,
                          DriverAssignmentRepository driverAssignmentRepo,
                          ConductorAssignmentRepository conductorAssignmentRepo) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.driverAssignmentRepo = driverAssignmentRepo;
        this.conductorAssignmentRepo = conductorAssignmentRepo;
    }

    // ✅ REGISTER (PASSENGER ONLY by default)
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req,
            @RequestParam(required = false) String role) {

        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Username already exists"));
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(encoder.encode(req.getPassword()));

        // Set role - default to PASSENGER; accept any valid enum (DRIVER, PASSENGER, CONDUCTOR, ADMIN)
        if (role != null) {
            try {
                u.setRole(Role.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                u.setRole(Role.PASSENGER); // Default role
            }
        } else {
            u.setRole(Role.PASSENGER); // Default role
        }

        userRepo.save(u);

        return ResponseEntity.ok(
            Map.of("message", "Registered successfully as " + u.getRole()));
    }

    // ✅ WHO AM I (USED BY REACT)
    @GetMapping("/whoami")
    public ResponseEntity<?> whoami(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                .body(Map.of("authenticated", false));
        }

        User u = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow();

        Bus assignedBus = null;
        try {
            // Try driver assignment first
            assignedBus = driverAssignmentRepo.findByDriverId(u.getId())
                    .map(DriverAssignment::getBus)
                    .orElse(null);
            if (assignedBus == null) {
                assignedBus = conductorAssignmentRepo.findByConductorId(u.getId())
                        .map(ConductorAssignment::getBus)
                        .orElse(null);
            }
        } catch (Exception ignored) {}

        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("authenticated", true);
        payload.put("username", u.getUsername());
        payload.put("role", u.getRole().name());
        // email not available on User model; omit from payload
        if (assignedBus != null) {
            payload.put("assignedBusId", assignedBus.getId());
            payload.put("assignedBusNumber", assignedBus.getBusNumber());
            payload.put("assignedBusName", assignedBus.getBusName());
        }
        return ResponseEntity.ok(payload);
    }

    // ✅ LOGIN
   @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {

        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                req.get("username"),
                req.get("password")
            )
        );

        return Map.of(
            "message", "Login successful",
            "username", auth.getName(),
            "roles", auth.getAuthorities()
        );
    }

    // ✅ UPDATE PROFILE (self-edit)
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody Map<String, String> req) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        User u = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        String newUsername = req.get("username");
        String newPassword = req.get("password");
        String roleStr = req.get("role");
        String assignedBusIdStr = req.get("assignedBusId");

        // Username change (optional)
        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(u.getUsername())) {
            if (userRepo.findByUsername(newUsername).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }
            u.setUsername(newUsername);
        }

        // Password change (optional) - basic validation
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password too short"));
            }
            u.setPassword(encoder.encode(newPassword));
        }

        // Role change: only allow if current user is ADMIN
        if (roleStr != null && !roleStr.isBlank()) {
            boolean isAdmin = u.getRole() == Role.ADMIN;
            if (!isAdmin) {
                return ResponseEntity.status(403).body(Map.of("error", "Role change not permitted"));
            }
            try {
                u.setRole(Role.valueOf(roleStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
        }

        userRepo.save(u);
        return ResponseEntity.ok(Map.of("message", "Profile updated", "username", u.getUsername()));
    }
}
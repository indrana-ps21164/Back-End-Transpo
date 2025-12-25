package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.RegisterRequest;
import com.Transpo.transpo.Role;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    public AuthController(UserRepository userRepo,
                          BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    // ✅ REGISTER (PASSENGER ONLY)
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req) {

        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Username already exists"));
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(encoder.encode(req.getPassword()));

        // 🔐 DEFAULT ROLE
        u.setRole(Role.PASSENGER);

        userRepo.save(u);

        return ResponseEntity.ok(
            Map.of("message", "Registered successfully"));
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

        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "username", u.getUsername(),
            "role", u.getRole().name()
        ));
    }
}

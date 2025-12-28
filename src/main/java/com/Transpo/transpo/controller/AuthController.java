package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.RegisterRequest;
import com.Transpo.transpo.Role;
import com.Transpo.transpo.model.User;
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

    public AuthController(UserRepository userRepo,
                          BCryptPasswordEncoder encoder,
                          AuthenticationManager authManager) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
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

        // Set role - default to PASSENGER, only allow PASSENGER or CONDUCTOR for registration
        if (role != null && (role.equalsIgnoreCase("CONDUCTOR") || role.equalsIgnoreCase("PASSENGER"))) {
            u.setRole(Role.valueOf(role.toUpperCase()));
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

        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "username", u.getUsername(),
            "role", u.getRole().name()
        ));
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
}
package com.Transpo.transpo.controller;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthLoginController {

    private final AuthenticationManager authManager;

    public AuthLoginController(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

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

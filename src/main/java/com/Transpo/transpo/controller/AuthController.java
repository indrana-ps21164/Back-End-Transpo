package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/auth")


public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?>register(@RequestBody User incoming){
        if (incoming.getUsername()== null|| incoming.getPassword()==null) {
           return ResponseEntity.badRequest().body(Map.of("error","username and password required"));
        }
        if (userRepo.findByUsername(incoming.getUsername()).isPresent()) {
           return ResponseEntity.badRequest().body(Map.of("error","username exists")); 
        }
        User u =new User();
        u.setUsername(incoming.getUsername());
        u.setPassword(encoder.encode(incoming.getPassword()));
        u.setRole("USER");
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("message","regisered"));
    }

    @GetMapping("/whoami")
    public ResponseEntity<?>whoami(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if(user == null){
            return ResponseEntity.status(401).body(Map.of("authrnticated",false));
        }
        return ResponseEntity.ok(Map.of("authenticated",true,"username",user.getUsername()));
    }
    
}

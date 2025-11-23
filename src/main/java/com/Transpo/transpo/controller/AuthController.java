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
    public Map<String,String>register(@RequestBody User incoming){
        if (incoming.getUsername()== null|| incoming.getPassword()==null) {
            throw new RuntimeException("username and password required");
        }
        if (userRepo.findByUsername(incoming.getUsername()).isPresent()) {
           throw new RuntimeException("Username already exists"); 
        }
        User user =new User();
        user.setUsername(incoming.getUsername());
        user.setPassword(encoder.encode(incoming.getPassword()));
        user.setRole("USER");
        userRepo.save(user);
        return Map.of("message","regisered");
    }
}

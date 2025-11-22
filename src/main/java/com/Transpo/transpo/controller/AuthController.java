package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller

public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user, Model model) {
        // basic check: username unique
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            model.addAttribute("user", user);
            return "register";
        }
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        model.addAttribute("message", "Registration successful! Please login.");
        return "login";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }
    @GetMapping({"/","/home"})
    public String homePage(){
        return "home";
    }
    
    
}

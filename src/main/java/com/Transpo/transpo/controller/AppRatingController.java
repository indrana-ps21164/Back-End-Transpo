package com.Transpo.transpo.controller;

import com.Transpo.transpo.model.AppRating;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.service.AppRatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class AppRatingController {
    private final AppRatingService service;
    public AppRatingController(AppRatingService service) { this.service = service; }

    @GetMapping
    public List<AppRating> list() { return service.findAll(); }

    @GetMapping("/me")
    public List<AppRating> mine(@AuthenticationPrincipal User currentUser) {
        return currentUser == null ? List.of() : service.findByUsername(currentUser.getUsername());
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User currentUser,
                                    @RequestBody Map<String, Object> body) {
        try {
            Integer rating = body.get("rating") == null ? null : Integer.valueOf(String.valueOf(body.get("rating")));
            String comment = body.get("comment") == null ? null : String.valueOf(body.get("comment"));
            AppRating saved = service.create(currentUser, rating, comment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
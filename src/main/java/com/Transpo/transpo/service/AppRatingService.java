package com.Transpo.transpo.service;

import com.Transpo.transpo.model.AppRating;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.AppRatingRepository;
import com.Transpo.transpo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppRatingService {
    private final AppRatingRepository repo;
    private final UserRepository userRepo;

    public AppRatingService(AppRatingRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public List<AppRating> findAll() { return repo.findAll(); }
    public List<AppRating> findByUsername(String username) { return repo.findByUsername(username); }

    @Transactional
    public AppRating create(User currentUser, Integer rating, String comment) {
        AppRating ar = new AppRating();
        ar.setUsername(currentUser != null ? currentUser.getUsername() : "Anonymous");
        // Fetch profile image from user table; add field if exists or leave null
        // Assuming User has a profileImageUrl; otherwise set null
        if (currentUser != null) {
            try {
                User full = userRepo.findById(currentUser.getId()).orElse(currentUser);
                try {
                    var m = full.getClass().getMethod("getProfileImageUrl");
                    Object v = m.invoke(full);
                    ar.setProfileImageUrl(v == null ? null : String.valueOf(v));
                } catch (Exception ignore) {}
            } catch (Exception ignore) {}
        }
        ar.setRating(rating);
        ar.setComment(comment);
        ar.validate();
        return repo.save(ar);
    }
}
package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.AppRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppRatingRepository extends JpaRepository<AppRating, Long> {
    List<AppRating> findByUsername(String username);
}
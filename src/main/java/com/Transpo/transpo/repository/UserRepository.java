package com.Transpo.transpo.repository;

import com.Transpo.transpo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
            Optional<User> findByUsername(String username);
}

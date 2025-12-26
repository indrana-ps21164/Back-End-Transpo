package com.Transpo.transpo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepo;

    public CustomUserDetailService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

    User u = userRepo.findByUsername(username)
        .orElseThrow(() ->
            new UsernameNotFoundException("User not found: " + username));

    return new org.springframework.security.core.userdetails.User(
        u.getUsername(),
        u.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
    );
}



}

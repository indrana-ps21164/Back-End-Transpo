package com.Transpo.transpo;

import com.Transpo.transpo.model.User;
import com.Transpo.transpo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapData implements CommandLineRunner {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    public BootstrapData(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // Create admin if not exists
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
            System.out.println("Created admin/admin123");
        }else {
            // If admin already exists, update the role
            User existingAdmin = userRepo.findByUsername("admin").get();
            existingAdmin.setRole(Role.ADMIN);
            userRepo.save(existingAdmin);
            System.out.println("Updated existing admin role to ROLE_ADMIN");
        }

        // Create conductor if not exists
        if (userRepo.findByUsername("conductor").isEmpty()) {
            User conductor = new User();
            conductor.setUsername("conductor");
            conductor.setPassword(encoder.encode("conductor123"));
            conductor.setRole(Role.CONDUCTOR);
            userRepo.save(conductor);
            System.out.println("Created conductor/conductor123");
        }else {
            // If conductor already exists, update the role
            User existingConductor = userRepo.findByUsername("conductor").get();
            existingConductor.setRole(Role.CONDUCTOR);
            userRepo.save(existingConductor);
            System.out.println("Updated existing conductor role to ROLE_CONDUCTOR");
        }
    }
}
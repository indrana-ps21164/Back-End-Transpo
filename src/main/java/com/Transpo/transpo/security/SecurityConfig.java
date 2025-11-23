package com.Transpo.transpo.security;

import com.Transpo.transpo.service.CustomUserDetailService;
import java.security.Security;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
public class SecurityConfig {

    private final CustomUserDetailService userDetailsService;

    public SecurityConfig(CustomUserDetailService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())//Disable if REST + form-based is OK for dev
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**","/login","/register","/css/**","/js/**").permitAll()
                .requestMatchers("/api/buses/**","/api/routes/**","/api/schedules/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/login")
                .permitAll()
            )
            .logout(logout -> logout.permitAll());

            //register AthenticationProvider
            http.authenticationProvider(authenticationProvider());
        return http.build();
    }

}

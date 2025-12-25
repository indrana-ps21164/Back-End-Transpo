package com.Transpo.transpo.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;


import com.Transpo.transpo.service.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailService userDetailsService;

    public SecurityConfig(CustomUserDetailService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/buses/**",
                "/api/routes/**",
                "/api/schedules/**").permitAll()

            .requestMatchers("/api/driver/**").hasRole("DRIVER")
            .requestMatchers("/api/conductor/**").hasRole("CONDUCTOR")

            .requestMatchers(HttpMethod.POST,
                "/api/buses/**",
                "/api/routes/**",
                "/api/schedules/**").hasRole("ADMIN")

            .requestMatchers(HttpMethod.PUT,
                "/api/buses/**",
                "/api/routes/**",
                "/api/schedules/**").hasRole("ADMIN")

            .requestMatchers(HttpMethod.DELETE,
                "/api/buses/**",
                "/api/routes/**",
                "/api/schedules/**").hasRole("ADMIN")

            .anyRequest().authenticated()
        )
        .logout(logout -> logout.permitAll());

    return http.build();
}

}

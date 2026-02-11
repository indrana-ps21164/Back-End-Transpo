package com.Transpo.transpo.security;

import com.Transpo.transpo.service.CustomUserDetailService;  // ADD THIS IMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(CustomUserDetailService userDetailsService,
                          AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Expose AuthenticationManager for constructor injection in controllers
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Rely on AuthenticationConfiguration to supply AuthenticationManager; no explicit DaoAuthenticationProvider

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API testing
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.List.of(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://localhost:5173",
                    "http://localhost:5174"
                ));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))
            
            // Disable default form login - IMPORTANT!
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic auth
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // Disable logout filter since we handle our own
            .logout(logout -> logout.disable())
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/buses/**",
                    "/api/routes/**",
                    "/api/schedules/**",
                    "/api/reservations/by-email").permitAll()
                
                // Reservation endpoints
                .requestMatchers(HttpMethod.POST, "/api/reservations/**")
                    .hasAnyRole("PASSENGER", "CONDUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/reservations/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/reservations/**")
                    .hasRole("ADMIN")
                
                // Admin-only endpoints
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
                
                // Driver endpoints
                .requestMatchers("/api/driver/**").hasRole("DRIVER")

                // Other role endpoints
            
                .requestMatchers("/api/conductor/**").hasRole("CONDUCTOR")
                
                // Any other request needs authentication
                .anyRequest().authenticated()
            )
            
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .sessionFixation().migrateSession()
                .maximumSessions(1)
            )
            
            // Add custom filter to handle /auth/login
        .addFilterBefore(new CustomLoginFilter(
            authenticationConfiguration.getAuthenticationManager(),
            securityContextRepository()), 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

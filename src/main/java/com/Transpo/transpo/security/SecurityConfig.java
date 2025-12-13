// package com.Transpo.transpo.security;

// import com.Transpo.transpo.service.CustomUserDetailService;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class SecurityConfig {

// private final CustomUserDetailService userDetailsService;

// public SecurityConfig(CustomUserDetailService userDetailsService) {
// this.userDetailsService = userDetailsService;
// }

// @Bean
// public BCryptPasswordEncoder passwordEncoder() { return new
// BCryptPasswordEncoder(); }

// @Bean
// public DaoAuthenticationProvider authenticationProvider() {
// DaoAuthenticationProvider p = new DaoAuthenticationProvider();
// p.setUserDetailsService(userDetailsService);
// p.setPasswordEncoder(passwordEncoder());
// return p;
// }

// @Bean
// public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// http
// .csrf(csrf -> csrf.disable()) // enable in production if using browser forms
// + CSRF tokens
// .authorizeHttpRequests(auth -> auth
// .requestMatchers("/auth/**", "/", "/index.html", "/login",
// "/register").permitAll()
// // admin-only endpoints
// .requestMatchers("/api/admin/**", "/api/buses/**", "/api/routes/**",
// "/api/schedules/**").hasRole("ADMIN")
// .anyRequest().authenticated()
// )
// .formLogin(form -> form
// .loginProcessingUrl("/login")
// .permitAll()
// )
// .logout(logout -> logout.permitAll());

// http.authenticationProvider(authenticationProvider());
// return http.build();
// }
// }

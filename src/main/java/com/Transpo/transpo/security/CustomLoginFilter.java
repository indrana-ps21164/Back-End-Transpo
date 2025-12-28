package com.Transpo.transpo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class CustomLoginFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomLoginFilter(AuthenticationManager authenticationManager,
                            SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Only process /auth/login POST requests
        if (!request.getRequestURI().equals("/auth/login") || 
            !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Parse request body
            Map<String, String> credentials = objectMapper.readValue(
                request.getInputStream(), 
                Map.class
            );
            
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            // Create security context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Save security context to session
            securityContextRepository.saveContext(context, request, response);

            // Create session if doesn't exist
            HttpSession session = request.getSession(true);
            
            // Set response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            
            Map<String, Object> responseBody = Map.of(
                "message", "Login successful",
                "username", authentication.getName(),
                "authenticated", true,
                "sessionId", session.getId()
            );
            
            objectMapper.writeValue(response.getWriter(), responseBody);

        } catch (AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), 
                Map.of("error", "Login failed", "message", e.getMessage()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), 
                Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
package com.example.practical_test.config;

import com.example.practical_test.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenInterceptor extends OncePerRequestFilter {
    
    @Autowired
    private AuthService authService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip authentication for public endpoints
        String path = request.getRequestURI();
        
        // Allow access to authentication endpoints (register and login)
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Allow access to Swagger/OpenAPI endpoints
        if (path.startsWith("/swagger") || 
            path.startsWith("/swagger-ui") || 
            path.startsWith("/swagger-ui.html") ||
            path.startsWith("/api-docs") || 
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/webjars") ||
            path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Missing Authorization header\"}");
            return;
        }
        
        // Extract token - handle "Bearer token" or "Bearer Bearer token" cases
        String token = authHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim(); // Remove "Bearer " prefix
        }
        // If there's still "Bearer " in the token (double Bearer case), remove it
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        
        if (token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Missing token\"}");
            return;
        }
        
        try {
            authService.getUserIdFromToken(token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid token\"}");
        }
    }
}


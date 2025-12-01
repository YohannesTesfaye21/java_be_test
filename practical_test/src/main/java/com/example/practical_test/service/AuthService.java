package com.example.practical_test.service;

import com.example.practical_test.model.AuthUser;
import com.example.practical_test.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private AuthUserRepository authUserRepository;
    
    @Autowired
    private TokenService tokenService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public String login(String username, String password) {
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }
        
        AuthUser user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        return tokenService.generateToken(user.getId());
    }
    
    public AuthUser register(String username, String password) {
        if (authUserRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        AuthUser user = new AuthUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return authUserRepository.save(user);
    }
    
    public Long getUserIdFromToken(String token) {
        if (!tokenService.isValidToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        return tokenService.getUserIdFromToken(token);
    }
}


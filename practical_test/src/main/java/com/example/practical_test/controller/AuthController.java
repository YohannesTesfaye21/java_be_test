package com.example.practical_test.controller;

import com.example.practical_test.dto.LoginRequest;
import com.example.practical_test.dto.LoginResponse;
import com.example.practical_test.dto.RegisterRequest;
import com.example.practical_test.dto.RegisterResponse;
import com.example.practical_test.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @Operation(summary = "Register a new user", description = "Create a new user account with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.practical_test.dto.ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            com.example.practical_test.model.AuthUser user = authService.register(request.getUsername(), request.getPassword());
            RegisterResponse response = new RegisterResponse(
                "User registered successfully", 
                user.getId(), 
                user.getUsername()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "BAD_REQUEST"));
        }
    }
    
    @Operation(summary = "User login", description = "Authenticate user and receive access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.practical_test.dto.ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "UNAUTHORIZED"));
        }
    }
}


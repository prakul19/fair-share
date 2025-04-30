package com.cg.fairshare.controller;

import com.cg.fairshare.dto.LoginRequest;
import com.cg.fairshare.dto.RegisterRequest;
import com.cg.fairshare.service.IAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for email: {}", loginRequest.getEmail());

        // Normalize email
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();

        try {
            String token = authService.login(loginRequest);  // Pass only the loginRequest
            logger.info("Token generated successfully for user: {}", normalizedEmail);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Login failed for email: {}. Error: {}", normalizedEmail, e.getMessage());
            return ResponseEntity.status(403).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            logger.info("User registered successfully: {}", registerRequest.getEmail());
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            logger.error("Registration failed for email: {}. Error: {}", registerRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(400).body("User registration failed");
        }
    }
}

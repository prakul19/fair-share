package com.cg.fairshare.controller;

import com.cg.fairshare.dto.*;
import com.cg.fairshare.response.ApiResponse;
import com.cg.fairshare.service.IAuthService;
import com.cg.fairshare.util.JwtUtil;
import com.cg.fairshare.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private IAuthService authService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req);
        return ResponseUtil.ok(token, "Login successful");
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseUtil.ok("User registered");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        authService.createAndSendToken(email);
        return ResponseUtil.ok("OTP sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getEmail(), req.getOtp(), req.getNewPassword());
        return ResponseUtil.ok("Password reset successful");
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest req) {

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        authService.changePasswordAuthenticated(email, req.getOldPassword(), req.getNewPassword());
        return ResponseUtil.ok("Password changed successfully");
    }
}

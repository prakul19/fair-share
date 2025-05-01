package com.cg.fairshare.service;

import com.cg.fairshare.dto.LoginRequest;
import com.cg.fairshare.dto.RegisterRequest;
import com.cg.fairshare.model.User;
import com.cg.fairshare.repository.UserRepository;
import com.cg.fairshare.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetService passwordResetService;  // new

    @Override
    public String login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(email);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @Override
    public User register(RegisterRequest request) {
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().trim().toLowerCase())
                .password(encryptedPassword)
                .build();
        return userRepository.save(user);
    }

    // ---- New methods below ----

    /**
     * Public API: generate an OTP, store it, and email it to the user.
     */
    @Override
    public void createAndSendToken(String email) {
        passwordResetService.createAndSendToken(email.trim().toLowerCase());
    }

    /**
     * Public API: verify OTP + email, then reset password and email confirmation.
     */
    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        passwordResetService.resetPassword(
                email.trim().toLowerCase(),
                otp,
                newPassword
        );
    }

    /**
     * Authenticated API: verify old password + token, then change to new password and email confirmation.
     */
    @Override
    public void changePasswordAuthenticated(String email, String oldPassword, String newPassword) {
        passwordResetService.changePasswordAuthenticated(
                email.trim().toLowerCase(),
                oldPassword,
                newPassword
        );
    }
}

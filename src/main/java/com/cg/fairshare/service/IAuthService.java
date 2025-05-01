package com.cg.fairshare.service;

import com.cg.fairshare.dto.LoginRequest;
import com.cg.fairshare.dto.RegisterRequest;
import com.cg.fairshare.model.User;

public interface IAuthService {
    String login(LoginRequest loginRequest);
    User register(RegisterRequest registerRequest);

    // new methods:
    void createAndSendToken(String email);
    void resetPassword(String email, String otp, String newPassword);
    void changePasswordAuthenticated(String email, String oldPassword, String newPassword);
}

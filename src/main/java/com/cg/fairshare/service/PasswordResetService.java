package com.cg.fairshare.service;

public interface PasswordResetService{
    void createAndSendToken(String email);
    void resetPassword(String email, String otp, String newPassword);
    void changePasswordAuthenticated(String email, String oldPassword, String newPassword);
}

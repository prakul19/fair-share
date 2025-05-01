package com.cg.fairshare.service;

import com.cg.fairshare.model.PasswordResetToken;
import com.cg.fairshare.model.User;
import com.cg.fairshare.repository.PasswordResetTokenRepository;
import com.cg.fairshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired private PasswordResetTokenRepository tokenRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900_000) + 100_000);
    }

    @Override
    public void createAndSendToken(String email){
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));
        tokenRepo.deleteByEmail(email);
        String otp = generateOtp();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setEmail(email);
        prt.setToken(otp);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        tokenRepo.save(prt);

        emailService.sendSimpleMessage(
                email,
                "Your FairShare OTP",
                "Your OTP is: " + otp + " (valid for 10 minutes)"
        );
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword){
        PasswordResetToken prt = tokenRepo.findByEmailAndToken(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }
        User user = userRepo.findByEmail(email).get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        tokenRepo.delete(prt);

        emailService.sendSimpleMessage(
                email,
                "FairShare Password Reset",
                "Your password has been successfully reset."
        );
    }

    @Override
    public void changePasswordAuthenticated(String email, String oldPassword, String newPassword) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        emailService.sendSimpleMessage(
                email,
                "FairShare Password Changed",
                "Your password has been updated."
        );
    }
}

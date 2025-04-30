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

    // Inject the interface rather than the concrete implementation
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (passwordEncoder.matches(password, user.getPassword())) {
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
                .email(request.getEmail())
                .password(encryptedPassword)
                .build();
        return userRepository.save(user);
    }
}

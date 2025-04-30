package com.cg.fairshare.service;

import com.cg.fairshare.dto.LoginRequest;
import com.cg.fairshare.dto.RegisterRequest;
import com.cg.fairshare.model.User;

public interface IAuthService {

    String login(LoginRequest loginRequest);  // Ensure the signature matches

    User register(RegisterRequest registerRequest);
}

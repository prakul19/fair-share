package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;

    @NotBlank(message = "Otp is required")
    @Size(min = 6,max = 6)
    private String otp;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$",message = "Password must have at least one lowercase, one uppercase, one digit and one special character. No whitespaces are allowed and should be 8-20 characters long")
    private String newPassword;
}

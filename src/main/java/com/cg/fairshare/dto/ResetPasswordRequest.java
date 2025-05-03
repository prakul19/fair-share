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
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", message = "Password must contain one letter and one number")
    private String newPassword;
}

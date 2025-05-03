package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotNull(message = "Name cannot be null")
    @Size(min = 4, message = "Name must be at least 4 characters long")
    @Pattern(regexp = "^[A-Z][A-Za-z\\s]*$",message = "Must start with letter capital and contain only letters")
    private String name;

    @NotNull(message = "Email cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$",message = "Email must have only one @ and no spaces are allowed")
    private String email;
    private String password;
}

package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class LoginRequest {

    @NotNull(message = "Email cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$",message = "Email must have only one @ and no spaces are allowed")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", message = "Password must contain one letter and one number")
    private String password;

//    public LoginRequest() {}
//
//    public LoginRequest(String email, String password) {
//        this.email = email;
//        this.password = password;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
}

package com.conor.taskmanager.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters")
    private String userName;

    @Email(message = "Invalid email address")
    @Size(max = 254, message = "Email is too long")
    private String email;

    @Size(min = 7, message = "Password must be at least 7 characters long")
    private String password;

    @NotBlank(message = "Password confirmation cannot be empty")
    private String passwordConfirm;
}
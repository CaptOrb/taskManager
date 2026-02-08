package com.conor.taskmanager.model;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {

    @NotBlank(message = "Username cannot be empty")
    private String userName;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}

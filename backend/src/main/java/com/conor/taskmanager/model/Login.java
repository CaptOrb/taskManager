package com.conor.taskmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {

    @NotBlank(message = "Username is required")
    private String userName;
    
    @NotBlank(message = "Password is required")
    private String password;
}

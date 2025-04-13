package com.conor.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String userName;
    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    public LoginResponse(String userName, String accessToken) {
        this.userName = userName;
        this.accessToken = accessToken;
    }
}

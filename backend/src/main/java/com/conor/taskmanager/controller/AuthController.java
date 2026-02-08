package com.conor.taskmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.RegisterRequest;
import com.conor.taskmanager.security.CustomUserDetails;
import com.conor.taskmanager.service.UserService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody Login body) {
        LoginResponse response = userService.login(body);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public String getCurrentUserName(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails.getUsername();
    }
}

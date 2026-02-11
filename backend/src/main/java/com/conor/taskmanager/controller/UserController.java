package com.conor.taskmanager.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.RegisterRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.service.UserService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

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

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody PasswordChangeRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Password changed successfully"));
    }

    @GetMapping("/user")
    public String getCurrentUserName(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getUsername();
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(currentUser);
    }
}

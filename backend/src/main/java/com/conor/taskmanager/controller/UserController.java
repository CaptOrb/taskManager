package com.conor.taskmanager.controller;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.service.UserService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;


    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty.");
        }
        if (userRepository.findByUserName(user.getUserName()) != null) {
            return ResponseEntity.badRequest().body("Username is already taken.");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email is already taken.");
        }
        if (user.getUserName() !=null && user.getUserName().length() < 3) {
            return ResponseEntity.badRequest().body("Username must be atleast 3 characters long.");
        }

        if (user.getPassword() !=null && user.getPassword().length() < 7) {
            return ResponseEntity.badRequest().body("Password must be atleast 7 characters long.");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        String token = jwtService.generateToken(user.getUserName());
        user.setJwtToken(token);

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login body) {
        try {
            LoginResponse response = userService.login(body);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Collections.singletonMap("error", "Invalid username or password"));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Collections.singletonMap("error", "Invalid username or password"));
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Collections.singletonMap("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/user")
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

}

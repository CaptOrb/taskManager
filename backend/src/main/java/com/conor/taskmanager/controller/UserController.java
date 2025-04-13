package com.conor.taskmanager.controller;

import java.time.Duration;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (user.getUserName() != null && user.getUserName().length() < 3) {
            return ResponseEntity.badRequest().body("Username must be at least 3 characters long.");
        }

        if (user.getPassword() != null && user.getPassword().length() < 7) {
            return ResponseEntity.badRequest().body("Password must be at least 7 characters long.");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Save user to the repository
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login body, HttpServletResponse httpResponse) {
        try {
            // Authenticate user (this part could use AuthenticationManager to verify
            // credentials)
            LoginResponse response = userService.login(body);

            // Generate access and refresh tokens
            String accessToken = jwtService.generateAccessToken(response.getUserName());
            String refreshToken = jwtService.generateRefreshToken(response.getUserName());

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // set true in PROD
                    .path("/api/auth/refresh-token")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict")
                    .build();

            // Add the cookie to the response header
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            LoginResponse loginResponse = new LoginResponse(response.getUserName(), accessToken);

            return ResponseEntity.ok(loginResponse);

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
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (refreshToken == null || !jwtService.validateToken(refreshToken, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String username = jwtService.extractUserName(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(username);

        LoginResponse response = new LoginResponse(username, newAccessToken);

        return ResponseEntity.ok(response);
    }
}

package com.conor.taskmanager.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;

  public User registerUser(User user) {
    validateUserRegistration(user);
    
    if (userRepository.findByUserName(user.getUserName()) != null) {
      throw new IllegalArgumentException("Username is already taken.");
    }
    
    if (userRepository.findByEmail(user.getEmail()) != null) {
      throw new IllegalArgumentException("Email is already taken.");
    }

    String encodedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encodedPassword);
    String token = jwtService.generateToken(user.getUserName());
    user.setJwtToken(token);

    return userRepository.save(user);
  }

  public LoginResponse login(Login loginRequest) {
    UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(
        loginRequest.getUserName(), loginRequest.getPassword());
    authManager.authenticate(authInputToken);
    
    String token = jwtService.generateToken(loginRequest.getUserName());
    User user = userRepository.findByUserName(loginRequest.getUserName());
    
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }
    
    user.setJwtToken(token);
    return new LoginResponse(user.getUserName(), token);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User findByUserName(String userName) {
    return userRepository.findByUserName(userName);
  }

  public User getCurrentUser(String username) {
    User user = userRepository.findByUserName(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }
    return user;
  }

  public boolean changePassword(String username, PasswordChangeRequest request) {
    User user = userRepository.findByUserName(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    if (request.getNewPassword() == null || request.getNewPassword().length() < 7) {
      throw new IllegalArgumentException("New password must be at least 7 characters long");
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new IllegalArgumentException("New password and confirmation password do not match");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    
    return true;
  }

  private void validateUserRegistration(User user) {
    if (user.getUserName() == null || user.getUserName().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be empty.");
    }
    if (user.getUserName().length() < 3) {
      throw new IllegalArgumentException("Username must be at least 3 characters long.");
    }
    if (user.getPassword() == null || user.getPassword().length() < 7) {
      throw new IllegalArgumentException("Password must be at least 7 characters long.");
    }
  }
}
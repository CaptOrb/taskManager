package com.conor.taskmanager.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.InvalidCredentialsException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
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

  @Transactional
  public LoginResponse registerUser(User user) {
    if (userRepository.existsByUserName(user.getUserName())) {
      throw new ValidationException("Username is already taken.");
    }
    
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new ValidationException("Email is already taken.");
    }
      if (!user.getPassword().equals(user.getPasswordConfirm())) {
      throw new ValidationException("New password and confirmation password do not match");
    }


    user.setPassword(passwordEncoder.encode(user.getPassword()));
    User savedUser = userRepository.save(user);
    
    String token = jwtService.generateToken(savedUser.getUserName());
    return new LoginResponse(savedUser.getUserName(), token);
  }

  public LoginResponse login(Login loginRequest) {
    User user = userRepository.findByUserName(loginRequest.getUserName());
    if (user == null) {
      user = userRepository.findByEmail(loginRequest.getUserName());
    }
    
    if (user == null) {
      throw new InvalidCredentialsException("Invalid username or password");
    }

    UsernamePasswordAuthenticationToken authInputToken = 
        new UsernamePasswordAuthenticationToken(user.getUserName(), loginRequest.getPassword());
    
    try {
      authManager.authenticate(authInputToken);
    } catch (org.springframework.security.core.AuthenticationException e) {
      throw new InvalidCredentialsException("Invalid username or password");
    }

    String token = jwtService.generateToken(user.getUserName());
    return new LoginResponse(user.getUserName(), token);
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public User findByUserName(String userName) {
    return userRepository.findByUserName(userName);
  }

  @Transactional(readOnly = true)
  public User getCurrentUser(String username) {
    User user = userRepository.findByUserName(username);
    if (user == null) {
      throw new UserNotFoundException("User not found");
    }
    return user;
  }

  @Transactional
  public void changePassword(String username, PasswordChangeRequest request) {
    User user = userRepository.findByUserName(username);
    if (user == null) {
      throw new UserNotFoundException("User not found");
    }

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new ValidationException("Current password is incorrect");
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new ValidationException("New password and confirmation password do not match");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }
}
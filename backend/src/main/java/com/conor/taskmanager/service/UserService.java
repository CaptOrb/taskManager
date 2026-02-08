package com.conor.taskmanager.service;

import com.conor.taskmanager.util.FieldErrorUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.conor.taskmanager.exception.InvalidCredentialsException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.RegisterRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final UserLookupService userLookupService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;

  @Transactional
  public LoginResponse registerUser(RegisterRequest request) {

    String userName = request.getUserName().trim();
    String email = request.getEmail().trim().toLowerCase();
    Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

    if (userRepository.existsByUserName(userName)) {
      FieldErrorUtils.addFieldError(fieldErrors, "userName", "Username is already taken");
    }
    if (userRepository.existsByEmail(email)) {
      FieldErrorUtils.addFieldError(fieldErrors, "email", "Email is already taken");
    }

    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      FieldErrorUtils.addFieldError(fieldErrors, "passwordConfirm", "Passwords do not match");
    }

    if (!fieldErrors.isEmpty()) {
      throw new ValidationException(fieldErrors);
    }

    User user = new User();
    user.setUserName(userName);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setUserRole("user");

    User savedUser = userRepository.save(user);
    String token = jwtService.generateToken(savedUser.getId(), savedUser.getUserName());
    return new LoginResponse(savedUser.getUserName(), token);
  }

  @Transactional(readOnly = true)
  public LoginResponse login(Login loginRequest) {
    User user = userRepository.findByUserNameOrEmail(loginRequest.getUserName())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

    UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(user.getUserName(),
        loginRequest.getPassword());
    try {
      authManager.authenticate(authInputToken);
    } catch (org.springframework.security.core.AuthenticationException e) {
      throw new InvalidCredentialsException("Invalid username or password");
    }

    String token = jwtService.generateToken(user.getId(), user.getUserName());
    return new LoginResponse(user.getUserName(), token);
  }

  @Transactional(readOnly = true)
  public User getCurrentUser(Long userId) {
    return userLookupService.getUserById(userId);
  }

  @Transactional
  public void changePassword(Long userId, PasswordChangeRequest request) {
    User user = userLookupService.getUserById(userId);

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

package com.conor.taskmanager.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
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
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user);

  }

  public LoginResponse login(@RequestBody Login body) {
    UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(body.getUserName(),
        body.getPassword());
    authManager.authenticate(authInputToken);
    String token = jwtService.generateToken(body.getUserName());
    User user = userRepository.findByUserName(body.getUserName());
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }
    user.setJwtToken(token);
    return new LoginResponse(user.getUserName(), token);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email);

  }

}
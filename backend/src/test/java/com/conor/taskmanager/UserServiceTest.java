package com.conor.taskmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.conor.taskmanager.exception.InvalidCredentialsException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, authManager, jwtService);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setUserName("testUser");
        user.setEmail("test@example.com");
        user.setPassword("plainPassword");
        user.setPasswordConfirm("plainPassword");

        when(userRepository.existsByUserName("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken("testUser")).thenReturn("mockedToken");

        LoginResponse response = userService.registerUser(user);

        assertEquals("testUser", response.getUserName());
        assertEquals("mockedToken", response.getJwtToken());
    }

    @Test
    void testLoginSuccess() {
        Login loginRequest = new Login("username", "password");
        User user = new User();
        user.setUserName("username");

        when(userRepository.findByUserNameOrEmail("username")).thenReturn(user);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtService.generateToken("username")).thenReturn("mockJwtToken");

        LoginResponse response = userService.login(loginRequest);

        assertEquals("username", response.getUserName());
        assertEquals("mockJwtToken", response.getJwtToken());
    }

    @Test
    void testLoginUserNotFound() {
        Login loginRequest = new Login("username", "password");

        when(userRepository.findByUserNameOrEmail("username")).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
        verify(authManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginInvalidCredentials() {
        Login loginRequest = new Login("username", "wrongPassword");
        User user = new User();
        user.setUserName("username");

        when(userRepository.findByUserNameOrEmail("username")).thenReturn(user);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User foundUser = userService.findByEmail("test@example.com");

        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testChangePassword_Success() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "newPassword123");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword("testUser", request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_UserNotFound() {
        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "newPassword123");

        when(userRepository.findByUserName("nonexistent")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.changePassword("nonexistent", request));
    }

    @Test
    void testChangePassword_CurrentPasswordIncorrect() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("wrongPassword", "newPassword123", "newPassword123");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(ValidationException.class, () -> userService.changePassword("testUser", request));
    }

    @Test
    void testChangePassword_PasswordsDontMatch() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "differentPassword");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.changePassword("testUser", request));
    }
}

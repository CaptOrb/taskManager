package com.conor.taskmanager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import com.conor.taskmanager.model.RegisterRequest;
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
        RegisterRequest request = new RegisterRequest();
        request.setUserName("testUser");
        request.setEmail("test@example.com");
        request.setPassword("plainPassword");
        request.setPasswordConfirm("plainPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUserName("testUser");
        savedUser.setEmail("test@example.com");

        when(userRepository.existsByUserName("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(1L, "testUser")).thenReturn("mockedToken");

        LoginResponse response = userService.registerUser(request);

        assertEquals("testUser", response.getUserName());
        assertEquals("mockedToken", response.getJwtToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUserPasswordMismatch() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("testUser");
        request.setEmail("test@example.com");
        request.setPassword("plainPassword");
        request.setPasswordConfirm("differentPassword");

        assertThrows(ValidationException.class, () -> userService.registerUser(request));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void testRegisterUserUsernameAndEmailAlreadyTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("testUser");
        request.setEmail("test@example.com");
        request.setPassword("plainPassword");
        request.setPasswordConfirm("plainPassword");

        when(userRepository.existsByUserName("testUser")).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.registerUser(request));

        assertEquals("Validation failed", exception.getMessage());
        assertEquals(2, exception.getErrors().size());
        assertTrue(exception.getErrors().contains("Username is already taken"));
        assertTrue(exception.getErrors().contains("Email is already taken"));
        assertEquals(1, exception.getFieldErrors().get("userName").size());
        assertEquals(1, exception.getFieldErrors().get("email").size());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void testRegisterUserEmailNormalisation() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("testUser");
        request.setEmail("Test@Example.com");
        request.setPassword("plainPassword");
        request.setPasswordConfirm("plainPassword");

        when(userRepository.existsByUserName("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        // ArgumentCaptor verifies the actual object passed to the repository
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(1L);
                    return u;
                });
        when(jwtService.generateToken(eq(1L), eq("testUser"))).thenReturn("mockedToken");

        LoginResponse response = userService.registerUser(request);

        User savedUser = userCaptor.getValue();

        assertEquals("testUser", savedUser.getUserName());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("mockedToken", response.getJwtToken());

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        Login loginRequest = new Login("username", "password");
        User user = new User();
        user.setId(1L);
        user.setUserName("username");

        // Use findByUserNameOrEmail instead of findByUserName
        when(userRepository.findByUserNameOrEmail("username")).thenReturn(Optional.of(user));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtService.generateToken(1L, "username")).thenReturn("mockJwtToken");

        LoginResponse response = userService.login(loginRequest);

        assertEquals("username", response.getUserName());
        assertEquals("mockJwtToken", response.getJwtToken());
    }

    @Test
    void testLoginUserNotFound() {
        Login loginRequest = new Login("username", "password");

        // Use findByUserNameOrEmail instead of separate findByUserName and findByEmail
        when(userRepository.findByUserNameOrEmail("username")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
        verify(authManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginInvalidCredentials() {
        Login loginRequest = new Login("username", "wrongPassword");
        User user = new User();
        user.setId(1L);
        user.setUserName("username");

        // Use findByUserNameOrEmail instead of findByUserName
        when(userRepository.findByUserNameOrEmail("username")).thenReturn(Optional.of(user));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
    }

    @Test
    void testChangePassword_Success() {
        User user = new User();
        user.setId(1L);
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(1L, request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_UserNotFound() {
        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "newPassword123");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changePassword(99L, request));
    }

    @Test
    void testChangePassword_CurrentPasswordIncorrect() {
        User user = new User();
        user.setId(1L);
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("wrongPassword", "newPassword123", "newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(ValidationException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void testChangePassword_PasswordsDontMatch() {
        User user = new User();
        user.setId(1L);
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "differentPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.changePassword(1L, request));
    }
}

package com.conor.taskmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.service.UserService;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setUserName("testUser");
        user.setEmail("test@example.com");
        user.setPassword("plainPassword");

        when(userRepository.findByUserName("testUser")).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(jwtService.generateToken("testUser")).thenReturn("mockedToken");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = userService.registerUser(user);

        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals("mockedToken", registeredUser.getJwtToken());
    }

    @Test
    void testLoginSuccess() {
        Login loginRequest = new Login("username", "password");
        User user = new User();
        user.setUserName("username");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtService.generateToken("username")).thenReturn("mockJwtToken");
        when(userRepository.findByUserName("username")).thenReturn(user);

        LoginResponse response = userService.login(loginRequest);

        assertEquals("username", response.getUserName());
        assertEquals("mockJwtToken", response.getJwtToken());
    }

    @Test
    void testLoginUserNotFound() {
        Login loginRequest = new Login("username", "password");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUserName("username")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.login(loginRequest));
    }

    @Test
    void testLoginInvalidCredentials() {
        Login loginRequest = new Login("username", "wrongPassword");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequest));
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

        boolean result = userService.changePassword("testUser", request);

        assertTrue(result);
    }

    @Test
    void testChangePassword_UserNotFound() {
        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "newPassword123");

        when(userRepository.findByUserName("nonexistent")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.changePassword("nonexistent", request));
    }

    @Test
    void testChangePassword_CurrentPasswordIncorrect() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("wrongPassword", "newPassword123", "newPassword123");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("testUser", request));
    }

    @Test
    void testChangePassword_NewPasswordTooShort() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "123", "123");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("testUser", request));
    }

    @Test
    void testChangePassword_PasswordsDontMatch() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", "newPassword123", "differentPassword");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("testUser", request));
    }

    @Test
    void testChangePassword_NewPasswordNull() {
        User user = new User();
        user.setUserName("testUser");
        user.setPassword("encodedOldPassword");

        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword", null, "newPassword123");

        when(userRepository.findByUserName("testUser")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("testUser", request));
    }
}

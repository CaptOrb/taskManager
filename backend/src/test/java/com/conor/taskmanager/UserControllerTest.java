package com.conor.taskmanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.conor.taskmanager.controller.UserController;
import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.UserDetailsService;
import com.conor.taskmanager.service.UserService;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Mock user details
        User testUser = new User();
        testUser.setUserName("1@1.com");
        testUser.setEmail("1@1.com");
        testUser.setPassword("password");

        when(userRepository.findByUserName("1@1.com")).thenReturn(testUser);

        when(jwtService.extractUserName(any())).thenReturn("1@1.com");
        when(jwtService.validateToken(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "1@1.com", password = "password", roles = { "user" }) // Simulate logged-in user
    public void getCurrentUser_whenUserExists_returnsUser() throws Exception {
        User testUser = new User();
        testUser.setUserName("1@1.com");
        testUser.setEmail("1@1.com");
        testUser.setUserRole("user");

        when(userService.getCurrentUser("1@1.com")).thenReturn(testUser);

        mockMvc.perform(get("/api/auth/current-user")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("1@1.com"))
                .andExpect(jsonPath("$.email").value("1@1.com"))
                .andExpect(jsonPath("$.userRole").value("user"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void getCurrentUserName_returnsAuthenticatedUsername() throws Exception {
        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = { "USER" })
    public void getCurrentUser_whenUserExists_returnsUserObject() throws Exception {
        User mockUser = new User();
        mockUser.setUserName("test@example.com");
        mockUser.setEmail("test@example.com");
        mockUser.setUserRole("User");

        when(userService.getCurrentUser("test@example.com")).thenReturn(mockUser);

        mockMvc.perform(get("/api/auth/current-user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userName").value("test@example.com"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    public void getCurrentUser_whenAuthenticatedButUserNotFound_returnsUnauthorized() throws Exception {
        when(userService.getCurrentUser("nonexistent@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/auth/current-user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void register_whenUserIsValid_returnsSuccessMessage() throws Exception {
        User newUser = new User();
        newUser.setUserName("testUser");
        newUser.setEmail("testUser@example.com");
        newUser.setPassword("password123");

        // Mock the behavior of userService
        when(userService.registerUser(any(User.class))).thenReturn(newUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUser\",\"email\":\"testUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    public void register_whenUsernameIsEmpty_returnsBadRequest() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username cannot be empty."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"\",\"email\":\"testUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username cannot be empty."));
    }

    @Test
    public void register_whenUsernameIsTaken_returnsBadRequest() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username is already taken."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"userName\":\"existingUser\",\"email\":\"newUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken."));
    }

    @Test
    public void register_whenEmailIsTaken_returnsBadRequest() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email is already taken."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"userName\":\"newUser\",\"email\":\"existingUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already taken."));
    }

    @Test
    public void register_whenUsernameIsTooShort_returnsBadRequest() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username must be at least 3 characters long."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"ab\",\"email\":\"testUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username must be at least 3 characters long."));
    }

    @Test
    public void register_whenPasswordIsTooShort_returnsBadRequest() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Password must be at least 7 characters long."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUser\",\"email\":\"testUser@example.com\",\"password\":\"short\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be at least 7 characters long."));
    }

    @Test
    public void login_whenCredentialsAreValid_returnsLoginResponse() throws Exception {
        Login loginRequest = new Login();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("password123");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setJwtToken("mockedJwtToken");

        when(userService.login(loginRequest)).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUser\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("mockedJwtToken"));
    }

    @Test
    public void login_whenUsernameNotFound_returnsUnauthorized() throws Exception {
        Login loginRequest = new Login();
        loginRequest.setUserName("invalidUser");
        loginRequest.setPassword("password123");

        when(userService.login(loginRequest)).thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"invalidUser\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    public void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
        String invalidCredentials = "{\"userName\":\"invalidUser\",\"password\":\"wrongPassword\"}";

        when(userService.login(any())).thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCredentials))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    public void login_whenUnexpectedErrorOccurs_returnsInternalServerError() throws Exception {
        Login loginRequest = new Login();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("password123");

        when(userService.login(loginRequest)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUser\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void changePassword_whenValidRequest_returnsSuccess() throws Exception {
        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).changePassword(eq("test@example.com"), any(PasswordChangeRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void changePassword_whenCurrentPasswordIncorrect_returnsBadRequest() throws Exception {
        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
                .thenThrow(new IllegalArgumentException("Current password is incorrect"));

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"wrongPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is incorrect"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void changePassword_whenNewPasswordTooShort_returnsBadRequest() throws Exception {
        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
                .thenThrow(new IllegalArgumentException("New password must be at least 7 characters long"));

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"123\",\"confirmPassword\":\"123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("New password must be at least 7 characters long"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void changePassword_whenPasswordsDontMatch_returnsBadRequest() throws Exception {
        when(userService.changePassword(eq("test@example.com"), any(PasswordChangeRequest.class)))
                .thenThrow(new IllegalArgumentException("New password and confirmation password do not match"));

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"differentPassword\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("New password and confirmation password do not match"));
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    public void changePassword_whenUserNotFound_returnsNotFound() throws Exception {
        when(userService.changePassword(eq("nonexistent@example.com"), any(PasswordChangeRequest.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
}

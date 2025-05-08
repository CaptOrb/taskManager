package com.conor.taskmanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.conor.taskmanager.controller.UserController;
import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.UserDetailsService;
import com.conor.taskmanager.service.UserService;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
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

        when(userRepository.findByUserName("test@example.com")).thenReturn(mockUser);

        when(jwtService.generateToken(mockUser.getUserName())).thenReturn("mockedToken");

        mockMvc.perform(get("/api/auth/current-user")
                .header("Authorization", "Bearer mocked.jwt.token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userName").value("test@example.com"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    public void getCurrentUser_whenAuthenticatedButUserNotFound_returnsUnauthorized() throws Exception {
        when(userRepository.findByUserName("nonexistent@example.com")).thenReturn(null);

        mockMvc.perform(get("/api/auth/current-user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void register_whenUserIsValid_returnsSuccessMessage() throws Exception {
        User newUser = new User();
        newUser.setUserName("testUser");
        newUser.setEmail("testUser@example.com");
        newUser.setPassword("password123");

        // Mock the behavior of userRepository
        when(userRepository.findByUserName(newUser.getUserName())).thenReturn(null);
        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(newUser.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(newUser.getUserName())).thenReturn("mockedToken");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUser\",\"email\":\"testUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void register_whenUsernameIsEmpty_returnsBadRequest() throws Exception {
        User newUser = new User();
        newUser.setUserName("");
        newUser.setEmail("testUser@example.com");
        newUser.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"\",\"email\":\"testUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username cannot be empty."));
    }

    @Test
    public void register_whenUsernameIsTaken_returnsBadRequest() throws Exception {
        User newUser = new User();
        newUser.setUserName("existingUser");
        newUser.setEmail("newUser@example.com");
        newUser.setPassword("password123");

        when(userRepository.findByUserName(newUser.getUserName())).thenReturn(newUser);

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
        User newUser = new User();
        newUser.setUserName("newUser");
        newUser.setEmail("existingUser@example.com");
        newUser.setPassword("password123");

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(newUser);

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
        User newUser = new User();
        newUser.setUserName("ab");
        newUser.setEmail("testUser@example.com");
        newUser.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"ab\",\"email\":\"testUser@example.com\",\"password\":\"password123\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username must be atleast 3 characters long."));
    }

    @Test
    public void register_whenPasswordIsTooShort_returnsBadRequest() throws Exception {
        User newUser = new User();
        newUser.setUserName("testUser");
        newUser.setEmail("testUser@example.com");
        newUser.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUser\",\"email\":\"testUser@example.com\",\"password\":\"short\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be atleast 7 characters long."));
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
}

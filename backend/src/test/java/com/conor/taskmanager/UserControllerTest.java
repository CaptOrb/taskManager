package com.conor.taskmanager;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.conor.taskmanager.controller.UserController;
import com.conor.taskmanager.exception.GlobalExceptionHandler;
import com.conor.taskmanager.exception.InvalidCredentialsException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.Login;
import com.conor.taskmanager.model.LoginResponse;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.RegisterRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.security.CustomUserDetails;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.CustomUserDetailsService;
import com.conor.taskmanager.service.UserService;

@WebMvcTest(controllers = UserController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
public class UserControllerTest {

        @MockitoBean
        private CustomUserDetailsService userDetailsService;

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtService jwtService;

        private CustomUserDetails createTestUserDetails(Long id, String username) {
                User user = new User();
                user.setId(id);
                user.setUserName(username);
                user.setEmail(username);
                user.setPassword("password");
                user.setUserRole("user");
                return new CustomUserDetails(user);
        }

        @Test
        public void getCurrentUser_whenUserExists_returnsUser() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                User testUser = new User();
                testUser.setUserName("1@1.com");
                testUser.setEmail("1@1.com");
                testUser.setUserRole("user");

                when(userService.getCurrentUser(1L)).thenReturn(testUser);

                mockMvc.perform(get("/api/auth/current-user")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userName").value("1@1.com"))
                                .andExpect(jsonPath("$.email").value("1@1.com"))
                                .andExpect(jsonPath("$.userRole").value("user"));
        }

        @Test
        public void getCurrentUserName_returnsAuthenticatedUsername() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "test@example.com");

                mockMvc.perform(get("/api/auth/user").with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("test@example.com"));
        }

        @Test
        public void getCurrentUser_whenAuthenticatedButUserNotFound_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(99L, "nonexistent@example.com");

                when(userService.getCurrentUser(99L))
                                .thenThrow(new UserNotFoundException("User not found"));

                mockMvc.perform(get("/api/auth/current-user").with(user(userDetails)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        public void register_whenUserIsValid_returnsLoginResponse() throws Exception {
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setUserName("testUser");
                loginResponse.setJwtToken("mockedToken");

                when(userService.registerUser(any(RegisterRequest.class))).thenReturn(loginResponse);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"testUser\",\"email\":\"testUser@example.com\",\"password\":\"password123\",\"passwordConfirm\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userName").value("testUser"))
                                .andExpect(jsonPath("$.jwtToken").value("mockedToken"));
        }

        @Test
        public void register_whenUsernameIsEmpty_returnsBadRequest() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"\",\"email\":\"testUser@example.com\",\"password\":\"password123\",\"passwordConfirm\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors",
                                                hasItems("Username must be between 3 and 32 characters")));
        }

        @Test
        public void register_whenUsernameIsTaken_returnsBadRequest() throws Exception {
                when(userService.registerUser(any(RegisterRequest.class)))
                                .thenThrow(new ValidationException(Map.of(
                                                "userName", List.of("Username is already taken"))));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"existingUser\",\"email\":\"newUser@example.com\",\"password\":\"password123\",\"passwordConfirm\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Username is already taken")))
                                .andExpect(jsonPath("$.fieldErrors.userName", hasItems("Username is already taken")));
        }

        @Test
        public void register_whenEmailIsTaken_returnsBadRequest() throws Exception {
                when(userService.registerUser(any(RegisterRequest.class)))
                                .thenThrow(new ValidationException(Map.of(
                                                "email", List.of("Email is already taken"))));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"newUser\",\"email\":\"existingUser@example.com\",\"password\":\"password123\",\"passwordConfirm\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Email is already taken")))
                                .andExpect(jsonPath("$.fieldErrors.email", hasItems("Email is already taken")));
        }

        @Test
        public void register_whenUsernameAndEmailAreTaken_returnsAllValidationErrors() throws Exception {
                when(userService.registerUser(any(RegisterRequest.class)))
                                .thenThrow(new ValidationException(Map.of(
                                                "userName", List.of("Username is already taken"),
                                                "email", List.of("Email is already taken"))));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"existingUser\",\"email\":\"existingUser@example.com\",\"password\":\"password123\",\"passwordConfirm\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors",
                                                hasItems("Username is already taken", "Email is already taken")))
                                .andExpect(jsonPath("$.fieldErrors.userName", hasItems("Username is already taken")))
                                .andExpect(jsonPath("$.fieldErrors.email", hasItems("Email is already taken")));
        }

        @Test
        public void register_whenUsernameIsTooShort_returnsBadRequest() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"ab\",\"email\":\"testUser@example.com\",\"password\":\"password123\",\"passwordConfirm\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors",
                                                hasItems("Username must be between 3 and 32 characters")));
        }

        @Test
        public void register_whenPasswordIsTooShort_returnsBadRequest() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"testUser\",\"email\":\"testUser@example.com\",\"password\":\"short\",\"passwordConfirm\":\"short\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors",
                                                hasItems("Password must be at least 7 characters long")));
        }

        @Test
        public void register_whenMultipleFieldsAreInvalid_returnsAllValidationErrors() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"ab\",\"email\":\"\",\"password\":\"123\",\"passwordConfirm\":\"\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems(
                                                "Username must be between 3 and 32 characters",
                                                "Password must be at least 7 characters long",
                                                "Password confirmation cannot be empty")))
                                .andExpect(jsonPath("$.fieldErrors.userName",
                                                hasItems("Username must be between 3 and 32 characters")))
                                .andExpect(jsonPath("$.fieldErrors.password",
                                                hasItems("Password must be at least 7 characters long")))
                                .andExpect(jsonPath("$.fieldErrors.passwordConfirm",
                                                hasItems("Password confirmation cannot be empty")));
        }

        @Test
        public void login_whenCredentialsAreValid_returnsLoginResponse() throws Exception {
                Login loginRequest = new Login();
                loginRequest.setUserName("testUser");
                loginRequest.setPassword("password123");

                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setUserName("testUser");
                loginResponse.setJwtToken("mockedJwtToken");

                when(userService.login(any(Login.class))).thenReturn(loginResponse);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"testUser\",\"password\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.jwtToken").value("mockedJwtToken"));
        }

        @Test
        public void login_whenInvalidCredentials_returnsUnauthorized() throws Exception {
                when(userService.login(any(Login.class)))
                                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"invalidUser\",\"password\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        }

        @Test
        public void login_whenUnexpectedErrorOccurs_returnsInternalServerError() throws Exception {
                when(userService.login(any(Login.class)))
                                .thenThrow(new RuntimeException("Unexpected error"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userName\":\"testUser\",\"password\":\"password123\"}"))
                                .andDo(print())
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }

        @Test
        public void changePassword_whenValidRequest_returnsSuccess() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "test@example.com");

                doNothing().when(userService).changePassword(eq(1L), any(PasswordChangeRequest.class));

                mockMvc.perform(post("/api/auth/change-password")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }

        @Test
        public void changePassword_whenCurrentPasswordIncorrect_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "test@example.com");

                doThrow(new ValidationException("Current password is incorrect"))
                                .when(userService)
                                .changePassword(eq(1L), any(PasswordChangeRequest.class));

                mockMvc.perform(post("/api/auth/change-password")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currentPassword\":\"wrongPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Current password is incorrect")));
        }

        @Test
        public void changePassword_whenNewPasswordTooShort_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "test@example.com");

                mockMvc.perform(post("/api/auth/change-password")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"123\",\"confirmPassword\":\"123\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors",
                                                hasItems("New password must be at least 7 characters long")));
        }

        @Test
        public void changePassword_whenPasswordsDontMatch_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "test@example.com");

                doThrow(new ValidationException("New password and confirmation password do not match"))
                                .when(userService)
                                .changePassword(eq(1L), any(PasswordChangeRequest.class));

                mockMvc.perform(post("/api/auth/change-password")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"differentPassword\"}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("New password and confirmation password do not match")));
        }

        @Test
        public void changePassword_whenUserNotFound_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(99L, "nonexistent@example.com");

                doThrow(new UserNotFoundException("User not found"))
                                .when(userService)
                                .changePassword(eq(99L), any(PasswordChangeRequest.class));

                mockMvc.perform(post("/api/auth/change-password")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("User not found"));
        }
}

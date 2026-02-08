package com.conor.taskmanager;

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

import com.conor.taskmanager.controller.AccountController;
import com.conor.taskmanager.exception.GlobalExceptionHandler;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.PasswordChangeRequest;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.security.CustomUserDetails;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.CustomUserDetailsService;
import com.conor.taskmanager.service.UserService;

@WebMvcTest(controllers = AccountController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
public class AccountControllerTest {

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

                mockMvc.perform(get("/api/account/current-user")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userName").value("1@1.com"))
                                .andExpect(jsonPath("$.email").value("1@1.com"))
                                .andExpect(jsonPath("$.userRole").value("user"));
        }

        @Test
        public void getCurrentUser_whenAuthenticatedButUserNotFound_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(99L, "nonexistent@example.com");

                when(userService.getCurrentUser(99L))
                                .thenThrow(new UserNotFoundException("User not found"));

                mockMvc.perform(get("/api/account/current-user").with(user(userDetails)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        public void changePassword_whenValidRequest_returnsSuccess() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "test@example.com");

                doNothing().when(userService).changePassword(eq(1L), any(PasswordChangeRequest.class));

                mockMvc.perform(post("/api/account/change-password")
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

                mockMvc.perform(post("/api/account/change-password")
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

                mockMvc.perform(post("/api/account/change-password")
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

                mockMvc.perform(post("/api/account/change-password")
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

                mockMvc.perform(post("/api/account/change-password")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123\",\"confirmPassword\":\"newPassword123\"}"))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("User not found"));
        }
}

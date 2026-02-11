package com.conor.taskmanager;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.conor.taskmanager.controller.TaskController;
import com.conor.taskmanager.exception.GlobalExceptionHandler;
import com.conor.taskmanager.exception.TaskNotFoundException;
import com.conor.taskmanager.exception.ForbiddenException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.Task.Priority;
import com.conor.taskmanager.model.Task.Status;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.security.CustomUserDetails;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.UserDetailsService;
import com.conor.taskmanager.service.TaskService;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest(TaskController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
public class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private TaskService taskService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Autowired
        private ObjectMapper objectMapper;

        private CustomUserDetails createTestUserDetails(Long id, String username) {
                User user = new User();
                user.setId(id);
                user.setUserName(username);
                user.setPassword("password");
                user.setUserRole("user");
                return new CustomUserDetails(user);
        }

        @Test
        public void getTasks_whenUserExists_returnsTaskList() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                Task task1 = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                Task task2 = new Task(2, "Task 2", "Description for Task 2", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));
                List<Task> mockTasks = new ArrayList<>();
                mockTasks.add(task1);
                mockTasks.add(task2);
                when(taskService.getTasksForUser(1L)).thenReturn(mockTasks);

                mockMvc.perform(get("/api/tasks").with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].title").value("Task 1"))
                                .andExpect(jsonPath("$[0].description").value("Description for Task 1"))
                                .andExpect(jsonPath("$[1].title").value("Task 2"))
                                .andExpect(jsonPath("$[1].description").value("Description for Task 2"));
        }

        @Test
        public void getTasks_whenUserDoesNotExist_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                when(taskService.getTasksForUser(1L))
                                .thenThrow(new UserNotFoundException("User not found"));

                mockMvc.perform(get("/api/tasks").with(user(userDetails)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        public void getTask_whenTaskExists_returnsTask() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                Task mockTask = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                when(taskService.getTaskById(1, 1L)).thenReturn(mockTask);

                mockMvc.perform(get("/api/tasks/1").with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Task 1"))
                                .andExpect(jsonPath("$.description").value("Description for Task 1"));
        }

        @Test
        public void getTask_whenTaskDoesNotExist_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                when(taskService.getTaskById(1, 1L))
                                .thenThrow(new TaskNotFoundException("Task not found"));

                mockMvc.perform(get("/api/tasks/1").with(user(userDetails)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Task not found"));
        }

        @Test
        public void getTask_whenUnauthorised_returnsForbidden() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                when(taskService.getTaskById(1, 1L))
                                .thenThrow(new ForbiddenException("You do not have permission to access this task"));

                mockMvc.perform(get("/api/tasks/1").with(user(userDetails)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message")
                                                .value("You do not have permission to access this task"));
        }

        @Test
        public void createTask_whenTitleIsEmpty_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                // Bean Validation will catch this before reaching the service
                Task newTask = new Task(null, "", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                mockMvc.perform(post("/api/tasks")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Title cannot be empty")));
        }

        @Test
        public void createTask_whenTitleIsTooLong_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                // Bean Validation will catch this before reaching the service
                String longTitle = "A".repeat(51); // 51 characters, exceeds max of 50
                Task newTask = new Task(null, longTitle, "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                mockMvc.perform(post("/api/tasks")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Title can only be 50 characters")));
        }

        @Test
        public void createTask_whenDescriptionIsEmpty_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                // Bean Validation will catch this before reaching the service
                Task newTask = new Task(null, "New Task", "", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                mockMvc.perform(post("/api/tasks")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Description cannot be empty")));
        }

        @Test
        public void createTask_whenDescriptionIsTooLong_returnsBadRequest() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                // Bean Validation will catch this before reaching the service
                String longDescription = "A".repeat(5001); // exceeds max of 5000
                Task newTask = new Task(null, "New Task", longDescription, Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                mockMvc.perform(post("/api/tasks")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors", hasItems("Description can only be 5000 characters")));
        }

        @Test
        public void createTask_whenValidData_createsTask() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                Task newTask = new Task(null, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                Task savedTask = new Task(1, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                when(taskService.createTask(any(Task.class), eq(1L))).thenReturn(savedTask);

                mockMvc.perform(post("/api/tasks")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("New Task"))
                                .andExpect(jsonPath("$.description").value("Task description"))
                                .andExpect(jsonPath("$.priority").value("MEDIUM"));
        }

        @Test
        public void updateTask_whenAuthorised_updatesAndReturnsTask() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "testUser");

                int taskId = 1;
                Task updatedTask = new Task(null, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));
                Task savedTask = new Task(taskId, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));

                when(taskService.updateTask(eq(taskId), any(Task.class), eq(1L))).thenReturn(savedTask);

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("New Title"))
                                .andExpect(jsonPath("$.description").value("New Description"));

                verify(taskService).updateTask(eq(taskId), any(Task.class), eq(1L));
        }

        @Test
        public void updateTask_whenTaskNotFound_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                int taskId = 1;
                Task updatedTask = new Task(null, "Updated Title", "Updated Description", Status.IN_PROGRESS,
                                Priority.HIGH,
                                LocalDateTime.now().plusDays(5));

                when(taskService.updateTask(eq(taskId), any(Task.class), eq(1L)))
                                .thenThrow(new TaskNotFoundException("Task not found"));

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Task not found"));
        }

        @Test
        public void updateTask_whenUnauthorised_returnsForbidden() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "testUser");

                int taskId = 1;
                Task updatedTask = new Task(null, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));

                when(taskService.updateTask(eq(taskId), any(Task.class), eq(1L)))
                                .thenThrow(new ForbiddenException("You do not have permission to update this task"));

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message")
                                                .value("You do not have permission to update this task"));
        }

        @Test
        public void deleteTask_whenAuthorised_deletesTask() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                doNothing().when(taskService).deleteTask(1, 1L);

                mockMvc.perform(delete("/api/tasks/1").with(user(userDetails)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Task deleted successfully"));

                verify(taskService).deleteTask(1, 1L);
        }

        @Test
        public void deleteTask_whenTaskNotFound_returnsNotFound() throws Exception {
                CustomUserDetails userDetails = createTestUserDetails(1L, "1@1.com");

                doThrow(new TaskNotFoundException("Task not found"))
                                .when(taskService).deleteTask(1, 1L);

                mockMvc.perform(delete("/api/tasks/1").with(user(userDetails)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Task not found"));
        }
}
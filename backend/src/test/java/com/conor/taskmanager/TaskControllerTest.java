package com.conor.taskmanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.conor.taskmanager.controller.TaskController;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.Task.Priority;
import com.conor.taskmanager.model.Task.Status;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.UserDetailsService;
import com.conor.taskmanager.service.TaskService;
import com.conor.taskmanager.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
public class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserRepository userRepo;

        @MockitoBean
        private PasswordEncoder passwordEncoder;

        @MockitoBean

        private AuthenticationManager authenticationManager;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private TaskRepository taskRepo;

        @MockitoBean
        private TaskService taskService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                User testUser = new User();
                testUser.setUserName("1@1.com");
                testUser.setEmail("1@1.com");
                testUser.setPassword("password");

                when(userRepo.findByUserName("1@1.com")).thenReturn(testUser);

                when(jwtService.extractUserName(any())).thenReturn("1@1.com");
                when(jwtService.validateToken(any(), any())).thenReturn(true);

        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void getTasks_whenUserExists_returnsTaskList() throws Exception {
                User mockUser = new User();
                mockUser.setUserName("1@1.com");

                Task task1 = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                Task task2 = new Task(2, "Task 2", "Description for Task 2", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));

                List<Task> mockTasks = new ArrayList<>();
                mockTasks.add(task1);
                mockTasks.add(task2);

                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.findByUser(mockUser)).thenReturn(mockTasks);

                mockMvc.perform(get("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].title").value("Task 1"))
                                .andExpect(jsonPath("$[0].description").value("Description for Task 1"))
                                .andExpect(jsonPath("$[1].title").value("Task 2"))
                                .andExpect(jsonPath("$[1].description").value("Description for Task 2"));
        }

        @Test
        public void getTasks_whenUserDoesNotExist_returnsForbidden() throws Exception {
                when(userRepo.findByUserName("testUser")).thenReturn(null);

                mockMvc.perform(get("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void getTask_whenUserExistsAndTaskExists_returnsTask() throws Exception {
                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);

                Task mockTask = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                mockTask.setUser(mockUser);

                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.findTaskByID(1)).thenReturn(mockTask);

                mockMvc.perform(get("/api/tasks/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$.title").value("Task 1"))
                                .andExpect(jsonPath("$.description").value("Description for Task 1"));
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void getTask_whenUserExistsButTaskDoesNot_exist() throws Exception {
                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);
                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.findTaskByID(1)).thenReturn(null);

                mockMvc.perform(get("/api/tasks/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void getTask_whenUnAuthorised() throws Exception {
                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);

                User otherUser = new User();
                otherUser.setUserName("2@2.com");

                Task mockTask = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                mockTask.setUser(otherUser);

                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.findTaskByID(1)).thenReturn(mockTask);

                mockMvc.perform(get("/api/tasks/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void createTask_whenTitleIsTooLong() throws Exception {

                Task newTask = new Task(1, "New Task That is exceeds the character limit for the task title",
                                "Task description", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));

                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);

                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.save(any(Task.class))).thenReturn(newTask);

                mockMvc.perform(post("/api/create/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(taskToJson(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(content().string("Title can only be 50 words."));
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void createTask_whenMessageIsTooLong() throws Exception {

                String longDescription = "This description is intentionally too long and exceeds the 500 word limit. " +
                                "This is just for testing purposes, to ensure that the system handles large descriptions correctly. "
                                +
                                "It should trigger a validation error because the length exceeds the 500 word limit, which is the constraint placed on this field. "
                                +
                                "The description must be truncated, and this should fail validation, throwing an error message. "
                                +
                                "This text is repeated to simulate a long description. ".repeat(10);
                Task newTask = new Task(1, "New Task", longDescription, Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);

                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.save(any(Task.class))).thenReturn(newTask);

                mockMvc.perform(post("/api/create/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(taskToJson(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(content().string("Description can only be 500 words."));
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void createTask_whenUserIsAuthenticated_createsTask() throws Exception {

                Task newTask = new Task(1, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);

                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.save(any(Task.class))).thenReturn(newTask);

                mockMvc.perform(post("/api/create/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(taskToJson(newTask)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.title").value("New Task"))
                                .andExpect(jsonPath("$.description").value("Task description"))
                                .andExpect(jsonPath("$.priority").value("MEDIUM"));
        }

        @Test
        @WithMockUser(username = "testUser", roles = { "USER" })
        public void updateExistingTask_whenAuthorized_updatesAndReturnsTask() throws Exception {
                int taskId = 1;
                User currentUser = new User();
                currentUser.setId(1L);
                currentUser.setUserName("testUser");

                Task existingTask = new Task();
                existingTask.setTitle("Old Title");
                existingTask.setDescription("Old Description");
                existingTask.setUser(currentUser);

                Task updatedTask = new Task();
                updatedTask.setTitle("New Title");
                updatedTask.setDescription("New Description");
                updatedTask.setStatus(Task.Status.IN_PROGRESS);
                updatedTask.setPriority(Task.Priority.HIGH);
                updatedTask.setDueDate(LocalDateTime.now().plusDays(2));
                updatedTask.setUser(currentUser);

                when(userRepo.findByUserName("testUser")).thenReturn(currentUser);
                when(taskRepo.findTaskByID(taskId)).thenReturn(existingTask);
                when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(updatedTask);

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("New Title"))
                                .andExpect(jsonPath("$.description").value("New Description"));

                verify(taskService, times(1)).updateTask(eq(taskId), any(Task.class));
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void updateExistingTask_whenTaskNotFound_returnsNotFound() throws Exception {
                int taskId = 1;
                Task updatedTask = new Task(taskId, "Updated Title", "Updated Description", Status.IN_PROGRESS,
                                Priority.HIGH,
                                LocalDateTime.now().plusDays(5));

                when(taskRepo.findTaskByID(taskId)).thenReturn(null);

                mockMvc.perform(put("/api/tasks/update/" + taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(taskToJson(updatedTask)))
                                .andExpect(status().isNotFound());

                verify(taskService, never()).updateTask(anyInt(), any(Task.class));
        }

        @Test
        @WithMockUser(username = "testUser", roles = { "USER" })
        public void updateExistingTask_whenUnauthorized_returnsForbidden() throws Exception {
                int taskId = 1;
                User currentUser = new User();
                currentUser.setId(1L);
                currentUser.setUserName("testUser");

                User otherUser = new User();
                otherUser.setId(2L); // Different user ID
                otherUser.setUserName("otherUser");

                Task existingTask = new Task();
                existingTask.setTitle("Old Title");
                existingTask.setDescription("Old Description");
                existingTask.setUser(otherUser); // Task belongs to a different user

                Task updatedTask = new Task();
                updatedTask.setTitle("New Title");
                updatedTask.setDescription("New Description");
                updatedTask.setStatus(Task.Status.IN_PROGRESS);
                updatedTask.setPriority(Task.Priority.HIGH);
                updatedTask.setDueDate(LocalDateTime.now().plusDays(2));

                when(userRepo.findByUserName("testUser")).thenReturn(currentUser);
                when(taskRepo.findTaskByID(taskId)).thenReturn(existingTask);

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isForbidden()); // Expecting 403
        }

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void deleteTask_whenUserIsAuthenticated_deletesTask() throws Exception {
                Task newTask = new Task(1, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                User mockUser = new User();
                mockUser.setUserName("1@1.com");
                mockUser.setId(1L);

                newTask.setUser(mockUser);
                when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
                when(taskRepo.findTaskByID(1)).thenReturn(newTask);
                doNothing().when(taskRepo).delete(newTask);

                mockMvc.perform(delete("/api/tasks/delete/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        private String taskToJson(Task task) {
                return String.format(
                                "{\"title\": \"%s\", \"description\": \"%s\", \"dueDate\": \"%s\", \"priority\": \"%s\"}",
                                task.getTitle(), task.getDescription(), task.getDueDate(), task.getPriority());
        }

}
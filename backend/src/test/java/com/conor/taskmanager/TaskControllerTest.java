package com.conor.taskmanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.conor.taskmanager.controller.TaskController;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.Task.Priority;
import com.conor.taskmanager.model.Task.Status;
import com.conor.taskmanager.security.JwtService;
import com.conor.taskmanager.security.SecurityConfig;
import com.conor.taskmanager.security.UserDetailsService;
import com.conor.taskmanager.service.TaskService;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
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

        @Test
        @WithMockUser(username = "1@1.com", password = "password", roles = { "user" })
        public void getTasks_whenUserExists_returnsTaskList() throws Exception {
                Task task1 = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                Task task2 = new Task(2, "Task 2", "Description for Task 2", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));
                List<Task> mockTasks = new ArrayList<>();
                mockTasks.add(task1);
                mockTasks.add(task2);
                when(taskService.getTasksForUser("1@1.com")).thenReturn(mockTasks);

                mockMvc.perform(get("/api/tasks"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].title").value("Task 1"))
                                .andExpect(jsonPath("$[0].description").value("Description for Task 1"))
                                .andExpect(jsonPath("$[1].title").value("Task 2"))
                                .andExpect(jsonPath("$[1].description").value("Description for Task 2"));
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void getTasks_whenUserDoesNotExist_returnsUnauthorised() throws Exception {
                when(taskService.getTasksForUser("1@1.com")).thenReturn(null);

                mockMvc.perform(get("/api/tasks"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void getTask_whenTaskExists_returnsTask() throws Exception {
                Task mockTask = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                when(taskService.getTaskById(1, "1@1.com")).thenReturn(mockTask);

                mockMvc.perform(get("/api/tasks/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Task 1"))
                                .andExpect(jsonPath("$.description").value("Description for Task 1"));
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void getTask_whenTaskDoesNotExist_returnsNotFound() throws Exception {
                when(taskService.getTaskById(1, "1@1.com")).thenReturn(null);

                mockMvc.perform(get("/api/tasks/1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void getTask_whenUnauthorised_returnsNotFound() throws Exception {
                when(taskService.getTaskById(1, "1@1.com")).thenReturn(null);

                mockMvc.perform(get("/api/tasks/1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void createTask_whenTitleIsTooLong_returnsBadRequest() throws Exception {
                Task newTask = new Task(null, "New Task That is exceeds the character limit for the task title",
                                "Task description", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));

                when(taskService.validateTaskFields(any(Task.class))).thenReturn("Title can only be 50 words.");

                mockMvc.perform(post("/api/create/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Title can only be 50 words."));
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void createTask_whenDescriptionIsTooLong_returnsBadRequest() throws Exception {
                String longDescription = "This description is intentionally too long and exceeds the 500 word limit. "
                                .repeat(10);
                Task newTask = new Task(null, "New Task", longDescription, Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                when(taskService.validateTaskFields(any(Task.class))).thenReturn("Description can only be 500 words.");

                mockMvc.perform(post("/api/create/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Description can only be 500 words."));
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void createTask_whenValidData_createsTask() throws Exception {
                Task newTask = new Task(null, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));
                Task savedTask = new Task(1, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM,
                                LocalDateTime.now().plusDays(1));

                when(taskService.validateTaskFields(any(Task.class))).thenReturn(null);
                when(taskService.createTask(any(Task.class), eq("1@1.com"))).thenReturn(savedTask);

                mockMvc.perform(post("/api/create/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newTask)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("New Task"))
                                .andExpect(jsonPath("$.description").value("Task description"))
                                .andExpect(jsonPath("$.priority").value("MEDIUM"));
        }

        @Test
        @WithMockUser(username = "testUser")
        public void updateTask_whenAuthorised_updatesAndReturnsTask() throws Exception {
                int taskId = 1;
                Task updatedTask = new Task(null, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));
                Task savedTask = new Task(taskId, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));

                when(taskService.validateTaskFields(any(Task.class))).thenReturn(null);
                when(taskService.updateTask(eq(taskId), any(Task.class), eq("testUser"))).thenReturn(savedTask);

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("New Title"))
                                .andExpect(jsonPath("$.description").value("New Description"));

                (taskService).updateTask(eq(taskId), any(Task.class), eq("testUser"));
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void updateTask_whenTaskNotFound_returnsNotFound() throws Exception {
                int taskId = 1;
                Task updatedTask = new Task(null, "Updated Title", "Updated Description", Status.IN_PROGRESS,
                                Priority.HIGH,
                                LocalDateTime.now().plusDays(5));

                when(taskService.validateTaskFields(any(Task.class))).thenReturn(null);
                when(taskService.updateTask(eq(taskId), any(Task.class), eq("1@1.com"))).thenReturn(null);

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "testUser")
        public void updateTask_whenUnauthorised_returnsNotFound() throws Exception {
                int taskId = 1;
                Task updatedTask = new Task(null, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                                LocalDateTime.now().plusDays(2));

                when(taskService.validateTaskFields(any(Task.class))).thenReturn(null);
                when(taskService.updateTask(eq(taskId), any(Task.class), eq("testUser"))).thenReturn(null);

                mockMvc.perform(put("/api/tasks/" + taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void deleteTask_whenAuthorised_deletesTask() throws Exception {
                when(taskService.deleteTask(1, "1@1.com")).thenReturn(true);

                mockMvc.perform(delete("/api/tasks/delete/1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Task deleted successfully."));

                (taskService).deleteTask(1, "1@1.com");
        }

        @Test
        @WithMockUser(username = "1@1.com")
        public void deleteTask_whenTaskNotFound_returnsNotFound() throws Exception {
                when(taskService.deleteTask(1, "1@1.com")).thenReturn(false);

                mockMvc.perform(delete("/api/tasks/delete/1"))
                                .andExpect(status().isNotFound());
        }
}
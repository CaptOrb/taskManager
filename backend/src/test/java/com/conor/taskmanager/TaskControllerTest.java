package com.conor.taskmanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

    @MockBean
    private UserRepository userRepo;

    @MockBean
    private PasswordEncoder passwordEncoder; 

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService; 

    @MockBean
    private JwtService jwtService; 

    @MockBean
    private TaskRepository taskRepo;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserDetailsService userDetailsService;

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
    @WithMockUser(username = "1@1.com", password = "password", roles = {"user"}) // Simulate logged-in user
    public void getTasks_whenUserExists_returnsTaskList() throws Exception {
        User mockUser = new User();
        mockUser.setUserName("1@1.com");
    
        Task task1 = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));
        Task task2 = new Task(2, "Task 2", "Description for Task 2", Status.IN_PROGRESS, Priority.HIGH, LocalDateTime.now().plusDays(2));
    
        List<Task> mockTasks = new ArrayList<>();
        mockTasks.add(task1);
        mockTasks.add(task2);
    
        // Mock repository responses
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
@WithMockUser(username = "1@1.com", password = "password", roles = {"user"})
public void getTask_whenUserExistsAndTaskExists_returnsTask() throws Exception {
    User mockUser = new User();
    mockUser.setUserName("1@1.com");
    mockUser.setId(1L);

    Task mockTask = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));
    mockTask.setUser(mockUser);

    when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
    when(taskRepo.findTaskByID(1)).thenReturn(mockTask);

    // Act & Assert
    mockMvc.perform(get("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.title").value("Task 1"))
            .andExpect(jsonPath("$.description").value("Description for Task 1"));
}

@Test
@WithMockUser(username = "1@1.com", password = "password", roles = {"user"})
public void getTask_whenUserExistsButTaskDoesNot_exist() throws Exception {
    User mockUser = new User();
    mockUser.setUserName("1@1.com");
    mockUser.setId(1L);
    when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser); 
    when(taskRepo.findTaskByID(1)).thenReturn(null);

    // Act & Assert
    mockMvc.perform(get("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
}

@Test
@WithMockUser(username = "1@1.com", password = "password", roles = {"user"})
public void getTask_whenUnAuthorised() throws Exception {
    User mockUser = new User();
    mockUser.setUserName("1@1.com");
    mockUser.setId(1L);

    User otherUser = new User();
    otherUser.setUserName("2@2.com");

    Task mockTask = new Task(1, "Task 1", "Description for Task 1", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));
    mockTask.setUser(otherUser);

    when(userRepo.findByUserName("1@1.com")).thenReturn(mockUser);
    when(taskRepo.findTaskByID(1)).thenReturn(mockTask);

    mockMvc.perform(get("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
}


@Test
@WithMockUser(username = "1@1.com", password = "password", roles = {"user"}) 
public void createTask_whenUserIsAuthenticated_createsTask() throws Exception {

    Task newTask = new Task(1, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));

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
@WithMockUser(username = "1@1.com", password = "password", roles = {"user"}) 
public void deleteTask_whenUserIsAuthenticated_deletesTask() throws Exception {
    Task newTask = new Task(1, "New Task", "Task description", Status.COMPLETED, Priority.MEDIUM, LocalDateTime.now().plusDays(1));

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
    return String.format("{\"title\": \"%s\", \"description\": \"%s\", \"dueDate\": \"%s\", \"priority\": \"%s\"}",
            task.getTitle(), task.getDescription(), task.getDueDate(), task.getPriority());
}

}
package com.conor.taskmanager;

import com.conor.taskmanager.exception.TaskNotFoundException;
import com.conor.taskmanager.exception.ForbiddenException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.Task.Priority;
import com.conor.taskmanager.model.Task.Status;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.service.TaskService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTasksForUser_whenUserExists_returnsTaskList() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        Task task1 = new Task(1, "Task 1", "Description 1", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        Task task2 = new Task(2, "Task 2", "Description 2", Status.COMPLETED, Priority.HIGH,
                LocalDateTime.now().plusDays(2));
        List<Task> tasks = Arrays.asList(task1, task2);

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findByUser(user)).thenReturn(tasks);

        List<Task> result = taskService.getTasksForUser(username);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getTasksForUser_whenUserDoesNotExist_throwsException() {

        String username = "nonexistent@test.com";
        when(userRepository.findByUserName(username)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> {
            taskService.getTasksForUser(username);
        });
    }

    @Test
    void getTaskById_whenTaskExistsAndUserOwnsIt_returnsTask() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        Task task = new Task(1, "Task 1", "Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        task.setUser(user);

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(task);

        Task result = taskService.getTaskById(1, username);

        assertNotNull(result);
        assertEquals("Task 1", result.getTitle());
    }

    @Test
    void getTaskById_whenUserDoesNotOwnTask_throwsException() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUserName("other@test.com");

        Task task = new Task(1, "Task 1", "Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        task.setUser(otherUser); // Different user owns this task

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(task);

        assertThrows(ForbiddenException.class, () -> {
            taskService.getTaskById(1, username);
        });
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_throwsException() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(null);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(1, username);
        });
    }

    @Test
    void createTask_whenValidData_createsAndReturnsTask() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        Task newTask = new Task(null, "New Task", "Description", Status.PENDING, Priority.MEDIUM,
                LocalDateTime.now().plusDays(1));
        Task savedTask = new Task(1, "New Task", "Description", Status.PENDING, Priority.MEDIUM,
                LocalDateTime.now().plusDays(1));
        savedTask.setUser(user);

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = taskService.createTask(newTask, username);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Task", result.getTitle());
    }

    @Test
    void createTask_whenInvalidTitle_throwsException() {

        String username = "test@test.com";
        User user = new User();
        user.setUserName(username);

        Task invalidTask = new Task(null, "", "Description", Status.PENDING, Priority.MEDIUM,
                LocalDateTime.now().plusDays(1));

        when(userRepository.findByUserName(username)).thenReturn(user);

        assertThrows(ValidationException.class, () -> {
            taskService.createTask(invalidTask, username);
        });
    }

    @Test
    void updateTask_whenTaskExistsAndUserOwnsIt_updatesAndReturnsTask() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        Task existingTask = new Task(1, "Old Title", "Old Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        existingTask.setUser(user);

        Task updatedTaskDetails = new Task(1, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                LocalDateTime.now().plusDays(2));

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(existingTask);
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(1, updatedTaskDetails, username);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertEquals(Priority.HIGH, result.getPriority());
    }

    @Test
    void updateTask_whenTaskDoesNotExist_throwsException() {

        String username = "test@test.com";
        User user = new User();
        user.setUserName(username);

        Task updatedTaskDetails = new Task(1, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                LocalDateTime.now().plusDays(2));

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(null);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(1, updatedTaskDetails, username);
        });
    }

    @Test
    void updateTask_whenUserDoesNotOwnTask_throwsException() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        User otherUser = new User();
        otherUser.setId(2L);

        Task existingTask = new Task(1, "Old Title", "Old Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        existingTask.setUser(otherUser); // Different user owns this

        Task updatedTaskDetails = new Task(1, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                LocalDateTime.now().plusDays(2));

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(existingTask);

        assertThrows(ForbiddenException.class, () -> {
            taskService.updateTask(1, updatedTaskDetails, username);
        });
    }

    @Test
    void deleteTask_whenTaskExistsAndUserOwnsIt_deletesTask() {

        String username = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setUserName(username);

        Task task = new Task(1, "Task", "Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        task.setUser(user);

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(task);
        doNothing().when(taskRepository).delete(task);

        taskService.deleteTask(1, username);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_throwsException() {
        
        String username = "test@test.com";
        User user = new User();
        user.setUserName(username);

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(taskRepository.findTaskByID(1)).thenReturn(null);

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(1, username);
        });
    }
}
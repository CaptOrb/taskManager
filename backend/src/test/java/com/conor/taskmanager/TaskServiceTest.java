package com.conor.taskmanager;

import com.conor.taskmanager.exception.TaskNotFoundException;
import com.conor.taskmanager.exception.ForbiddenException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.Task.Priority;
import com.conor.taskmanager.model.Task.Status;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.service.TaskService;
import com.conor.taskmanager.service.UserLookupService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserLookupService userLookupService;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userLookupService);
    }

    @Test
    void getTasksForUser_whenUserExists_returnsTaskList() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        Task task1 = new Task(1, "Task 1", "Description 1", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        Task task2 = new Task(2, "Task 2", "Description 2", Status.COMPLETED, Priority.HIGH,
                LocalDateTime.now().plusDays(2));
        List<Task> tasks = Arrays.asList(task1, task2);

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findByUser(user)).thenReturn(tasks);

        List<Task> result = taskService.getTasksForUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getTasksForUser_whenUserDoesNotExist_throwsException() {

        Long userId = 99L;
        when(userLookupService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> {
            taskService.getTasksForUser(userId);
        });
    }

    @Test
    void getTaskById_whenTaskExistsAndUserOwnsIt_returnsTask() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        Task task = new Task(1, "Task 1", "Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        task.setUser(user);

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1, userId);

        assertNotNull(result);
        assertEquals("Task 1", result.getTitle());
    }

    @Test
    void getTaskById_whenUserDoesNotOwnTask_throwsException() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUserName("other@test.com");

        Task task = new Task(1, "Task 1", "Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        task.setUser(otherUser); // Different user owns this task

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> {
            taskService.getTaskById(1, userId);
        });
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_throwsException() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(1, userId);
        });
    }

    @Test
    void createTask_whenValidData_createsAndReturnsTask() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        Task newTask = new Task(null, "New Task", "Description", Status.PENDING, Priority.MEDIUM,
                LocalDateTime.now().plusDays(1));
        Task savedTask = new Task(1, "New Task", "Description", Status.PENDING, Priority.MEDIUM,
                LocalDateTime.now().plusDays(1));
        savedTask.setUser(user);

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = taskService.createTask(newTask, userId);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Task", result.getTitle());
    }

    @Test
    void updateTask_whenTaskExistsAndUserOwnsIt_updatesAndReturnsTask() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        Task existingTask = new Task(1, "Old Title", "Old Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        existingTask.setUser(user);

        Task updatedTaskDetails = new Task(1, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                LocalDateTime.now().plusDays(2));

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(1, updatedTaskDetails, userId);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertEquals(Priority.HIGH, result.getPriority());
    }

    @Test
    void updateTask_whenTaskDoesNotExist_throwsException() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        Task updatedTaskDetails = new Task(1, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                LocalDateTime.now().plusDays(2));

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(1, updatedTaskDetails, userId);
        });
    }

    @Test
    void updateTask_whenUserDoesNotOwnTask_throwsException() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        User otherUser = new User();
        otherUser.setId(2L);

        Task existingTask = new Task(1, "Old Title", "Old Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        existingTask.setUser(otherUser); // Different user owns this

        Task updatedTaskDetails = new Task(1, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH,
                LocalDateTime.now().plusDays(2));

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));

        assertThrows(ForbiddenException.class, () -> {
            taskService.updateTask(1, updatedTaskDetails, userId);
        });
    }

    @Test
    void deleteTask_whenTaskExistsAndUserOwnsIt_deletesTask() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        Task task = new Task(1, "Task", "Description", Status.PENDING, Priority.LOW,
                LocalDateTime.now().plusDays(1));
        task.setUser(user);

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        taskService.deleteTask(1, userId);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_throwsException() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserName("test@test.com");

        when(userLookupService.getUserById(userId)).thenReturn(user);
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(1, userId);
        });
    }
}
package com.conor.taskmanager;


import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.Task.Priority;
import com.conor.taskmanager.model.Task.Status;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.service.TaskService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void updateTask_whenTaskExists_updatesAndReturnsUpdatedTask() {
        int taskId = 1;
        Task existingTask = new Task(taskId, "Old Title", "Old Description", Status.PENDING, Priority.LOW, LocalDateTime.now().plusDays(1));
        Task updatedTaskDetails = new Task(taskId, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH, LocalDateTime.now().plusDays(2));

        when(taskRepository.findTaskByID(taskId)).thenReturn(existingTask);
        when(taskRepository.save(existingTask)).thenReturn(updatedTaskDetails);

        Task result = taskService.updateTask(taskId, updatedTaskDetails);

        assertNotNull(result);
        assertEquals(updatedTaskDetails.getTitle(), result.getTitle());
        assertEquals(updatedTaskDetails.getDescription(), result.getDescription());
        assertEquals(updatedTaskDetails.getStatus(), result.getStatus());
        assertEquals(updatedTaskDetails.getPriority(), result.getPriority());
        assertEquals(updatedTaskDetails.getDueDate(), result.getDueDate());

        verify(taskRepository).findTaskByID(taskId);
        verify(taskRepository).save(existingTask);
    }

    @Test
    void updateTask_whenTaskDoesNotExist_returnsNull() {
        int taskId = 1;
        Task updatedTaskDetails = new Task(taskId, "New Title", "New Description", Status.IN_PROGRESS, Priority.HIGH, LocalDateTime.now().plusDays(2));

        when(taskRepository.findTaskByID(taskId)).thenReturn(null);

        Task result = taskService.updateTask(taskId, updatedTaskDetails);

        assertEquals(null, result);

        verify(taskRepository).findTaskByID(taskId);
        verify(taskRepository, org.mockito.Mockito.never()).save(org.mockito.Mockito.any(Task.class));
    }
}
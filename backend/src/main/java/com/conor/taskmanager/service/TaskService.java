package com.conor.taskmanager.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.TaskNotFoundException;
import com.conor.taskmanager.exception.ForbiddenException;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserLookupService userLookupService;

    @Transactional(readOnly = true)
    public List<Task> getTasksForUser(String username) {
        User user = userLookupService.getUserByUsername(username);
        return taskRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Integer id, String username) {
        User user = userLookupService.getUserByUsername(username);
        return getTaskByIdAndVerifyOwnership(id, user);
    }

    @Transactional
    public Task createTask(Task task, String username) {
        User user = userLookupService.getUserByUsername(username);

        task.setUser(user);
        task.setStatus(Task.Status.PENDING);
        task.setPriority(Task.Priority.LOW);

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Integer id, Task updatedTask, String username) {
        User user = userLookupService.getUserByUsername(username);
        Task existingTask = getTaskByIdAndVerifyOwnership(id, user);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());

        if (shouldResetReminder(existingTask, updatedTask)) {
            existingTask.setReminderSentAt(null);
        }

        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existingTask);
    }

    @Transactional
    public void deleteTask(Integer id, String username) {
        User user = userLookupService.getUserByUsername(username);
        Task task = getTaskByIdAndVerifyOwnership(id, user);
        taskRepository.delete(task);
    }

    private boolean shouldResetReminder(Task existingTask, Task updatedTask) {
        boolean dueDateChanged = !Objects.equals(existingTask.getDueDate(), updatedTask.getDueDate());
        boolean taskReopened = existingTask.getStatus() == Task.Status.COMPLETED
                            && updatedTask.getStatus() != Task.Status.COMPLETED;
        return dueDateChanged || taskReopened;
    }

    private Task getTaskByIdAndVerifyOwnership(Integer id, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        verifyTaskOwnership(task, user);
        return task;
    }

    private void verifyTaskOwnership(Task task, User user) {
        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this task");
        }
    }
}
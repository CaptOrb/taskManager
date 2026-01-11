package com.conor.taskmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.TaskNotFoundException;
import com.conor.taskmanager.exception.ForbiddenException;
import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Task> getTasksForUser(String username) {
        User user = getUserByUsername(username);
        return taskRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Integer id, String username) {
        User user = getUserByUsername(username);
        Task task = taskRepository.findTaskByID(id);
        if (task == null) {
            throw new TaskNotFoundException("Task not found.");
        }

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this task.");
        }

        return task;
    }

    @Transactional
    public Task createTask(Task task, String username) {
        User user = getUserByUsername(username);
        validateTask(task);

        task.setUser(user);
        task.setStatus(Task.Status.PENDING);
        task.setPriority(Task.Priority.LOW);

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Integer id, Task updatedTask, String username) {
        User user = getUserByUsername(username);
        Task existingTask = taskRepository.findTaskByID(id);

        if (existingTask == null) {
            throw new TaskNotFoundException("Task not found.");
        }

        if (!existingTask.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to update this task.");
        }

        validateTask(updatedTask);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existingTask);
    }

    @Transactional
    public void deleteTask(Integer id, String username) {
        User user = getUserByUsername(username);
        Task task = taskRepository.findTaskByID(id);

        if (task == null) {
            throw new TaskNotFoundException("Task not found.");
        }

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this task.");
        }

        taskRepository.delete(task);
    }

    private void validateTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new ValidationException("Title cannot be empty.");
        }
        if (task.getTitle().length() > 50) {
            throw new ValidationException("Title can only be 50 characters.");
        }
        if (task.getDescription() == null || task.getDescription().strip().isEmpty()) {
            throw new ValidationException("Description cannot be empty.");
        }
        if (task.getDescription().length() > 500) {
            throw new ValidationException("Description can only be 500 characters.");
        }
    }

    private User getUserByUsername(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return user;
    }
}
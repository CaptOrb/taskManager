package com.conor.taskmanager.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.TaskNotFoundException;
import com.conor.taskmanager.exception.ForbiddenException;
import com.conor.taskmanager.exception.UserNotFoundException;
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
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this task");
        }

        return task;
    }

    @Transactional
    public Task createTask(Task task, String username) {
        User user = getUserByUsername(username);

        task.setUser(user);
        task.setStatus(Task.Status.PENDING);
        task.setPriority(Task.Priority.LOW);

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Integer id, Task updatedTask, String username) {
        User user = getUserByUsername(username);
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!existingTask.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to update this task");
        }

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());

        if (!Objects.equals(existingTask.getDueDate(), updatedTask.getDueDate())
                || (existingTask.getStatus() == Task.Status.COMPLETED && updatedTask.getStatus() != Task.Status.COMPLETED)) {
            existingTask.setReminderSentAt(null);
        }

        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existingTask);
    }

    @Transactional
    public void deleteTask(Integer id, String username) {
        User user = getUserByUsername(username);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this task");
        }

        taskRepository.delete(task);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
package com.conor.taskmanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.repository.UserRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getTasksForUser(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return null;
        }
        return taskRepository.findByUser(user);
    }

    public Task getTaskById(Integer id, String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return null;
        }

        Task task = taskRepository.findTaskByID(id);
        if (task == null) {
            return null;
        }

        if (!task.getUser().getId().equals(user.getId())) {
            return null;
        }

        return task;
    }

    public Task createTask(Task task, String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return null;
        }

        if (!isTaskValid(task)) {
            return null;
        }

        task.setUser(user);
        return taskRepository.save(task);
    }

    public Task updateTask(Integer id, Task updatedTask, String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return null;
        }

        Task existingTask = taskRepository.findTaskByID(id);

        if (existingTask == null) {
            return null;
        }

        if (!existingTask.getUser().getId().equals(user.getId())) {
            return null;
        }

        if (!isTaskValid(updatedTask)) {
            return null;
        }

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existingTask);
    }

    public boolean deleteTask(Integer id, String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return false;
        }

        Task task = taskRepository.findTaskByID(id);
        if (task == null) {
            return false;
        }

        if (!task.getUser().getId().equals(user.getId())) {
            return false;
        }

        taskRepository.delete(task);
        return true;
    }

    public String validateTaskFields(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            return "Title cannot be empty.";
        }
        if (task.getTitle().length() > 50) {
            return "Title can only be 50 words.";
        }
        if (task.getDescription() == null || task.getDescription().strip().isEmpty()) {
            return "Description cannot be empty.";
        }
        if (task.getDescription().length() > 500) {
            return "Description can only be 500 words.";
        }
        return null;
    }

    private boolean isTaskValid(Task task) {
        return validateTaskFields(task) == null;
    }
}
package com.conor.taskmanager.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping(value = "/api/tasks", produces = "application/json")
    public ResponseEntity<List<Task>> getTasks(Authentication authentication) {
        List<Task> tasks = taskService.getTasksForUser(authentication.getName());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping(value = "/api/tasks/{id}", produces = "application/json")
    public ResponseEntity<Task> getTask(@PathVariable int id, Authentication authentication) {
        Task task = taskService.getTaskById(id, authentication.getName());
        return ResponseEntity.ok(task);
    }

    @PostMapping(value = "/api/tasks", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        Task savedTask = taskService.createTask(task, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @DeleteMapping(value = "/api/tasks/{id}", produces = "application/json")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable int id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.ok(Collections.singletonMap("message", "Task deleted successfully"));
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable int id, @Valid @RequestBody Task updatedTask, Authentication authentication) {
        Task task = taskService.updateTask(id, updatedTask, authentication.getName());
        return ResponseEntity.ok(task);
    }
}
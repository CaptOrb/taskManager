package com.conor.taskmanager.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.security.CustomUserDetails;
import com.conor.taskmanager.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping(value = "/api/tasks", produces = "application/json")
    public ResponseEntity<List<Task>> getTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Task> tasks = taskService.getTasksForUser(userDetails.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping(value = "/api/tasks/{id}", produces = "application/json")
    public ResponseEntity<Task> getTask(@PathVariable int id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Task task = taskService.getTaskById(id, userDetails.getId());
        return ResponseEntity.ok(task);
    }

    @PostMapping(value = "/api/tasks", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Task savedTask = taskService.createTask(task, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @DeleteMapping(value = "/api/tasks/{id}", produces = "application/json")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable int id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getId());
        return ResponseEntity.ok(Collections.singletonMap("message", "Task deleted successfully"));
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable int id, @Valid @RequestBody Task updatedTask, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Task task = taskService.updateTask(id, updatedTask, userDetails.getId());
        return ResponseEntity.ok(task);
    }
}
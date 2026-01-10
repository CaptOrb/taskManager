package com.conor.taskmanager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.service.TaskService;

@RestController
@CrossOrigin
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping(value = "/api/tasks", produces = "application/json")
    public ResponseEntity<?> getTasks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        List<Task> tasks = taskService.getTasksForUser(username);
        
        if (tasks == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        return ResponseEntity.ok(tasks);
    }

    @GetMapping(value = "/api/tasks/{id}", produces = "application/json")
    public ResponseEntity<?> getTask(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Task task = taskService.getTaskById(id, username);
        if (task == null) {
            Map<String, String> error = Map.of("error", "Task not found or unauthorised");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        return ResponseEntity.ok(task);
    }

    @PostMapping(value = "/api/create/task", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        String validationError = taskService.validateTaskFields(task);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }
        
        Task savedTask = taskService.createTask(task, username);
        
        if (savedTask == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised: Please log in.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @DeleteMapping(value = "/api/tasks/delete/{id}", produces = "application/json")
    public ResponseEntity<?> deleteTask(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        boolean deleted = taskService.deleteTask(id, username);
        
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found or unauthorised.");
        }

        return ResponseEntity.ok("Task deleted successfully.");
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable int id, @RequestBody Task updatedTask) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String validationError = taskService.validateTaskFields(updatedTask);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }
    
        Task task = taskService.updateTask(id, updatedTask, username);
        
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found or unauthorised.");
        }
        return ResponseEntity.ok(task);
    }
}
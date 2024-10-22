package com.conor.taskmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.repository.UserRepository;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    public TaskController() {}

    @Autowired
    TaskRepository taskRepo;

    @Autowired
    UserRepository userRepo;

    @GetMapping(value = "/api/tasks", produces = "application/json")
    public ResponseEntity<List<Task>> getTasks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByUserName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<Task> tasks = taskRepo.findByUser(currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @GetMapping(value = "/api/tasks/{id}", produces = "application/json")
    public ResponseEntity<Task> getTask(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByUserName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Task task = taskRepo.findTaskByID(id);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!currentUser.equals(task.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        return ResponseEntity.ok(task);
    }

    @PostMapping(value = "/api/create/task", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByUserName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Title cannot be empty.");
        }

        if (task.getDescription() == null || task.getDescription().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Description cannot be empty.");
        }

        task.setUser(currentUser);

        Task savedTask = taskRepo.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

}

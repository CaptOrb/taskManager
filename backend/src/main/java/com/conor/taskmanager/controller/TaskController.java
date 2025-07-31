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
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.service.TaskService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

    public TaskController() {
    }

    @Autowired
    TaskRepository taskRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    TaskService taskService;

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
    public ResponseEntity<?> getTask(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByUserName(username);

        if (currentUser == null) {
            Map<String, String> error = Map.of("error", "Unauthorised");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        Task task = taskRepo.findTaskByID(id);

        if (task == null) {
            Map<String, String> error = Map.of("error", "Task not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        if (!currentUser.getId().equals(task.getUser().getId())) {
            Map<String, String> error = Map.of("error", "Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        return ResponseEntity.ok(task);
    }

    @PostMapping(value = "/api/create/task", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByUserName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised: Please log in.");
        }

        ResponseEntity<?> validationResponse = validateTaskFields(task);
        if (validationResponse != null) {
            return validationResponse;
        }

        task.setUser(currentUser);
        Task savedTask = taskRepo.save(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @DeleteMapping(value = "/api/tasks/delete/{id}", produces = "application/json")
    public ResponseEntity<?> deleteTask(@PathVariable int id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByUserName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised: Please log in.");
        }

        Task task = taskRepo.findTaskByID(id);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found.");
        }

        if (!currentUser.getId().equals(task.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this task.");
        }

        taskRepo.delete(task);

        return ResponseEntity.status(HttpStatus.OK).body("Task deleted successfully.");
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable int id, @RequestBody Task updatedTask) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepo.findByUserName(username);

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised: Please log in.");
            }

            Task task = taskRepo.findTaskByID(id);

            if (!currentUser.getId().equals(task.getUser().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorised to update this task.");
            }

            ResponseEntity<?> validationResponse = validateTaskFields(updatedTask);
            if (validationResponse != null) {
                return validationResponse;
            }

            Task existingTask = taskService.updateTask(id, updatedTask);

            return ResponseEntity.ok(existingTask);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    private ResponseEntity<?> validateTaskFields(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Title cannot be empty.");
        }

        if (task.getTitle().length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Title can only be 50 words.");
        }

        if (task.getDescription() == null || task.getDescription().strip().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Description cannot be empty.");
        }

        if (task.getDescription().length() > 500) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Description can only be 500 words.");
        }
        return null;
    }

}

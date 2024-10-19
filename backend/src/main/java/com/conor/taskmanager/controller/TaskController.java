package com.conor.taskmanager.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.repository.TaskRepository;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {

  public TaskController() {
  }

  @Autowired
  TaskRepository taskRepo;

  @GetMapping(value = "/api/tasks", produces = "application/json")
  public ResponseEntity<ArrayList<Task>> getTasks() {
    taskRepo.save(new Task(19, "title", "description", "ready"));
    ArrayList<Task> list = new ArrayList<>();

    for (Task description : taskRepo.findAll()) {
      list.add(description);

    }
    return ResponseEntity.status(HttpStatus.OK).body(list);
  }

}

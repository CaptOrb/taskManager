package com.conor.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.repository.TaskRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task updateTask(int id, Task updatedTask) {
        Task existingTask = taskRepository.findTaskByID(id);

        if (existingTask == null) {
            return null;
        }
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existingTask);
    }

}

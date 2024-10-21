package com.conor.taskmanager.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Task {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) 
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 250, nullable = false)
    private String description;

    @Column(nullable = false)
    private Status status = Status.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.LOW;

    @Column
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime createdDate;

    public Task() {
        this.createdDate = LocalDateTime.now();
        this.status = Status.NOT_STARTED;
    }

    public Task(int id, String title, String description, Status status, Priority priority, LocalDateTime dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority != null ? priority : Priority.LOW; 
        this.dueDate = dueDate;
        this.createdDate = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,   
     }
}

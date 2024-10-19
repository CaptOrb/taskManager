package com.conor.taskmanager.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Task {

    public Task(int id, String title, String description, String status){
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
      
    }

    public Task(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String description;
    private String status;

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

}

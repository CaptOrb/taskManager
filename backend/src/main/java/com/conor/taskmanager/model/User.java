package com.conor.taskmanager.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 45)
	private String email;

	@Column(nullable = false, length = 64)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@Transient
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String passwordConfirm;

	@Column(name = "user_id", nullable = false, length = 20)
	private String userName;

	@Column(nullable = false, length = 64)
	private String userRole = "user";

	@Transient
	private String jwtToken;

	@JsonIgnore 
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Task> tasks = new ArrayList<>(); 

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPasswordConfirm(String matchingPassword) {
		this.passwordConfirm = matchingPassword;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String username) {
		this.userName = username;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void addTask(Task task) {
		tasks.add(task);
		task.setUser(this);
	}

	public void removeTask(Task task) {
		tasks.remove(task);
		task.setUser(null); 
	}

}

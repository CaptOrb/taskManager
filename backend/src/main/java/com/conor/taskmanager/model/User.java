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

@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(nullable = false, length = 64)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@Column(name = "user_name", nullable = false, unique = true, length = 32)
	private String userName;

	@Column(nullable = false, length = 64)
	private String userRole = "user";

	@Column(name = "ntfy_enabled", nullable = false)
	@JsonIgnore
	private boolean ntfyEnabled = false;

	@Column(name = "ntfy_topic", length = 128)
	@JsonIgnore
	private String ntfyTopic;

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

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public boolean isNtfyEnabled() {
		return ntfyEnabled;
	}

	public String getNtfyTopic() {
		return ntfyTopic;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void setNtfyEnabled(boolean ntfyEnabled) {
		this.ntfyEnabled = ntfyEnabled;
	}

	public void setNtfyTopic(String ntfyTopic) {
		this.ntfyTopic = ntfyTopic;
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

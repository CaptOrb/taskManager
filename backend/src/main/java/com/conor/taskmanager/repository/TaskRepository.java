package com.conor.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

	@Query("select t FROM Task t WHERE t.id = ?1")
	public Task findTaskByID(String id);

	List<Task> findByUser(User user);
}
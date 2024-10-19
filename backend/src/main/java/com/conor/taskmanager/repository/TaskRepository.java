package com.conor.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.conor.taskmanager.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

	@Query("select t FROM Task t WHERE t.id = ?1")
	public Task findTaskByID(String id);

}
package com.conor.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

	List<Task> findByUser(User user);
}
package com.conor.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.conor.taskmanager.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);

	@Query("SELECT u FROM User u WHERE u.userName = ?1 OR u.email = ?1")
	public User findByUserNameOrEmail(String identifier);
}
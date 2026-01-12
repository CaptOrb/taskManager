package com.conor.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.conor.taskmanager.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
}
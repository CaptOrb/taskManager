package com.conor.taskmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.conor.taskmanager.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    // use entity class name not table name
    @Query("SELECT u FROM User u WHERE u.userName = :identifier OR u.email = :identifier")
    Optional<User> findByUserNameOrEmail(@Param("identifier") String identifier);
}
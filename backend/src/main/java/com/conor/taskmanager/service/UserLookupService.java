package com.conor.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

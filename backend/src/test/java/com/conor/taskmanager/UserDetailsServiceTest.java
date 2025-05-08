package com.conor.taskmanager;


import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;
import com.conor.taskmanager.security.UserDetailsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_userFound() {
        String username = "testuser";
        String password = "password";
        User user = new User();
        user.setUserName(username);
        user.setPassword(password);

        when(userRepository.findByUserName(username)).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_userNotFound() {
        String username = "nonexistentuser";
        when(userRepository.findByUserName(username)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));
    }
}
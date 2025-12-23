package com.conor.taskmanager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.conor.taskmanager.security.JwtService;

import static org.mockito.Mockito.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtService jwtService;

    @Test
    public void testExtractUserName() {
        String mockToken = "valid-jwt-token";
        String expectedUserName = "1@1.com";

        when(jwtService.extractUserName(mockToken)).thenReturn(expectedUserName);

        String userName = jwtService.extractUserName(mockToken);

        assertEquals(expectedUserName, userName);

        verify(jwtService, times(1)).extractUserName(mockToken);
    }

    @Test
    public void testExtractExpiration() {
        String mockToken = "valid-jwt-token";
        Date expectedExpiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

        when(jwtService.extractExpiration(mockToken)).thenReturn(expectedExpiration);

        Date expiration = jwtService.extractExpiration(mockToken);

        assertEquals(expectedExpiration, expiration);

        verify(jwtService, times(1)).extractExpiration(mockToken);
    }

}

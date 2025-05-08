package com.conor.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.conor.taskmanager.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

@SpringBootTest
@ActiveProfiles("test")  // Use 'test' profile for loading 'application-test.yml'
class JwtServiceTest {

    private JwtService jwtService;

    @Value("${jwt.secret}")  // Inject JWT secret from application-test.yml
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            System.err.println("JWT Secret is NULL or EMPTY!");
        } else {
            System.out.println("JWT Secret from YAML: " + jwtSecret);
        }
    
        // Pass the plain secret (do not encode it again)
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            ReflectionTestUtils.setField(jwtService, "secret", jwtSecret);
        }
    }
    

    @Test
    void testGenerateAndValidateToken() {
        String username = "testuser";

        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUserName(token));
        assertFalse(jwtService.isTokenExpired(token));

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(username);

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        String username = "testuser";

        String token = jwtService.generateToken(username);
        Date originalExpiration = jwtService.extractExpiration(token);

        assertFalse(jwtService.isTokenExpired(token));
        assertTrue(originalExpiration.after(new Date()));
    }
}

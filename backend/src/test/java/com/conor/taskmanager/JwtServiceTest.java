package com.conor.taskmanager;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.conor.taskmanager.security.JwtService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"; // Must be at
                                                                                                          // least 256
                                                                                                          // bits

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
    }

    @Test
    void testGenerateToken_ShouldCreateValidToken() {
        String username = "test@example.com";

        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void testExtractUserName_ShouldReturnCorrectUsername() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        String extractedUsername = jwtService.extractUserName(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractExpiration_ShouldReturnFutureDate() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        Instant expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.isAfter(Instant.now()), "Expiration should be in the future");
    }

    @Test
    void testExtractExpiration_ShouldBeApproximatelyOneHour() {
        String username = "test@example.com";
        Instant beforeGeneration = Instant.now();

        String token = jwtService.generateToken(username);
        Instant expiration = jwtService.extractExpiration(token);

        long expectedExpiration = beforeGeneration.plusSeconds(60 * 60).toEpochMilli(); // 1 hour
        long actualExpiration = expiration.toEpochMilli();
        long tolerance = 5000; // 5 seconds tolerance

        assertTrue(Math.abs(actualExpiration - expectedExpiration) < tolerance,
                "Expiration should be approximately 1 hour from token generation");
    }

    @Test
    void testIsTokenExpired_WithValidToken_ShouldReturnFalse() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        Boolean isExpired = jwtService.isTokenExpired(token);

        assertFalse(isExpired, "Newly generated token should not be expired");
    }

    @Test
    void testValidateToken_WithValidTokenAndMatchingUser_ShouldReturnTrue() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .build();

        Boolean isValid = jwtService.validateToken(token, userDetails);

        assertTrue(isValid, "Token should be valid for matching user");
    }

    @Test
    void testValidateToken_WithDifferentUsername_ShouldReturnFalse() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);
        UserDetails userDetails = User.builder()
                .username("different@example.com")
                .password("password")
                .build();

        Boolean isValid = jwtService.validateToken(token, userDetails);

        assertFalse(isValid, "Token should not be valid for different user");
    }

    @Test
    void testExtractUserName_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUserName(invalidToken);
        });
    }

    @Test
    void testExtractUserName_WithTamperedToken_ShouldThrowSignatureException() {
        String username = "test@example.com";
        String validToken = jwtService.generateToken(username);
        // Tamper with the token by changing a character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        assertThrows(SignatureException.class, () -> {
            jwtService.extractUserName(tamperedToken);
        });
    }
}

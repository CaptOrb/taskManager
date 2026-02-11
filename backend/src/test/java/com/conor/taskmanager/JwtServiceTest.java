package com.conor.taskmanager;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.conor.taskmanager.model.User;
import com.conor.taskmanager.security.CustomUserDetails;
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
        Long userid = 1L;
        String token = jwtService.generateToken(userid, "testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void testExtractUserId_ShouldReturnCorrectUserId() {
        Long userid = 1L;
        String token = jwtService.generateToken(userid, "testuser");

        Long extractedId = jwtService.extractUserId(token);

        assertEquals(userid, extractedId);
    }

    @Test
    void testExtractExpiration_ShouldReturnFutureDate() {
        Long userid = 1L;
        String token = jwtService.generateToken(userid, "testuser");

        Instant expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.isAfter(Instant.now()), "Expiration should be in the future");
    }

    @Test
    void testExtractExpiration_ShouldBeApproximatelyOneHour() {
        Long userid = 1L;
        Instant beforeGeneration = Instant.now();

        String token = jwtService.generateToken(userid, "testuser");
        Instant expiration = jwtService.extractExpiration(token);

        long expectedExpiration = beforeGeneration.plusSeconds(60 * 60).toEpochMilli(); // 1 hour
        long actualExpiration = expiration.toEpochMilli();
        long tolerance = 5000; // 5 seconds tolerance

        assertTrue(Math.abs(actualExpiration - expectedExpiration) < tolerance,
                "Expiration should be approximately 1 hour from token generation");
    }

    @Test
    void testIsTokenExpired_WithValidToken_ShouldReturnFalse() {
        Long userid = 1L;
        String token = jwtService.generateToken(userid, "testuser");

        Boolean isExpired = jwtService.isTokenExpired(token);

        assertFalse(isExpired, "Newly generated token should not be expired");
    }

    @Test
    void testValidateToken_WithValidTokenAndMatchingUser_ShouldReturnTrue() {
        Long userId = 1L;

        String token = jwtService.generateToken(userId, "testuser");

        User user = new User();
        user.setId(userId);
        user.setUserName("test@example.com");
        user.setPassword("password");
        user.setUserRole("user");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Boolean isValid = jwtService.validateToken(token, userDetails);

        assertTrue(isValid, "Token should be valid for matching user");
    }

    @Test
    void testValidateToken_WithDifferentUserId_ShouldReturnFalse() {
        Long userId = 1L;
        String token = jwtService.generateToken(userId, "testuser");

        User user = new User();
        user.setId(2L);
        user.setUserName("different@example.com");
        user.setPassword("password");
        user.setUserRole("user");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Boolean isValid = jwtService.validateToken(token, userDetails);

        assertFalse(isValid, "Token should not be valid for different user");
    }

    @Test
    void testExtractUserId_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUserId(invalidToken);
        });
    }

    @Test
    void testExtractUserId_WithTamperedToken_ShouldThrowSignatureException() {
        Long userId = 1L;
        String validToken = jwtService.generateToken(userId, "testuser");
        // Tamper with the token by changing a character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        assertThrows(SignatureException.class, () -> {
            jwtService.extractUserId(tamperedToken);
        });
    }
}

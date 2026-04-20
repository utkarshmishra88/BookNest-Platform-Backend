package com.booknest.auth.util;

import com.booknest.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Use a 256-bit secret key for HMAC-SHA256
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySuperSecretKeyForJwtWhichIsLongEnough32Bytes!");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 3600000L); // 1 hour
    }

    @Test
    void testGenerateAndExtractToken() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("testuser@example.com");
        user.setFullName("Test User");
        user.setRole("ROLE_USER");
        
        String token = jwtUtil.generateToken(user);

        assertNotNull(token);

        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals("testuser@example.com", extractedEmail);

        String role = jwtUtil.extractRole(token);
        assertEquals("ROLE_USER", role);

        Integer userId = jwtUtil.extractUserId(token);
        assertEquals(1, userId);
        
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalidToken"));
    }
}

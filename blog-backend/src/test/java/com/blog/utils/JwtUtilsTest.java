package com.blog.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();
        // Set fields via reflection
        setField("secret", "mySecretKeyForTestingPurposesThatIsLongEnough12345678");
        setField("expiration", 86400000L);       // 24h
        setField("refreshExpiration", 604800000L); // 7d
        jwtUtils.init();
    }

    private void setField(String name, Object value) throws Exception {
        Field field = JwtUtils.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtUtils, value);
    }

    // ========== generate + parse ==========

    @Test
    void generateAccessToken_parseReturnsCorrectUserId() {
        String token = jwtUtils.generateAccessToken(42L, "zhangsan");
        assertEquals(42L, jwtUtils.getUserId(token));
        assertEquals("zhangsan", jwtUtils.getUsername(token));
    }

    @Test
    void generateRefreshToken_isRefreshType() {
        String token = jwtUtils.generateRefreshToken(1L);
        assertTrue(jwtUtils.isRefreshToken(token));
    }

    @Test
    void accessToken_isNotRefreshToken() {
        String token = jwtUtils.generateAccessToken(1L, "user");
        assertFalse(jwtUtils.isRefreshToken(token));
    }

    // ========== validation ==========

    @Test
    void isTokenValid_validToken() {
        String token = jwtUtils.generateAccessToken(1L, "user");
        assertTrue(jwtUtils.isTokenValid(token));
    }

    @Test
    void isTokenValid_tamperedToken() {
        String token = jwtUtils.generateAccessToken(1L, "user");
        assertFalse(jwtUtils.isTokenValid(token + "x"));
    }

    @Test
    void validateRefreshToken_validRefreshToken() {
        String token = jwtUtils.generateRefreshToken(99L);
        assertEquals(99L, jwtUtils.validateRefreshToken(token));
    }

    @Test
    void validateRefreshToken_accessToken_throws() {
        String token = jwtUtils.generateAccessToken(1L, "user");
        assertThrows(RuntimeException.class, () -> jwtUtils.validateRefreshToken(token));
    }

    // ========== generateToken backward compat ==========

    @Test
    void generateToken_equalsAccessToken() {
        String token = jwtUtils.generateToken(5L, "test");
        assertEquals(5L, jwtUtils.getUserId(token));
        assertEquals("test", jwtUtils.getUsername(token));
    }
}

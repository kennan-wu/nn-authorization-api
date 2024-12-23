package com.kennan.mp3player.mp3_player_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.kennan.mp3player.mp3_player_api.model.User;

@SpringBootTest
public class JwtServiceTest {
    
    @Autowired
    private JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    private String testToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        User user = new User("1", "testUser", "test@example.com", "password");
        testUser = user;

        String token = jwtService.generateToken(user);
        testToken = token;
    }

    @Test
    void testGenerateToken() {
        assertNotNull(testToken);
    }

    @Test
    void testExtractUsername() {
        String username = jwtService.extractUsername(testToken);
        assertEquals(username, testUser.getUsername());
    }

    @Test
    void testIsTokenValidWithSameUser() {
        boolean isValid = jwtService.isTokenValid(testToken, testUser);
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidAfterTime() {
        JwtService spyService = spy(jwtService);
        doReturn(new Date(System.currentTimeMillis() + expiration - 1000)).when(spyService).getCurrentDate();
        boolean isValid = spyService.isTokenValid(testToken, testUser);
        assertTrue(isValid);
    }

    @Test
    void testIsTokenInalidWithAnotherUser() {
        User anotherUser = new User("2", "anotherTestUser", "anotherUser@example.com", "anotherPassword");
        boolean isValid = jwtService.isTokenValid(testToken, anotherUser);
        assertFalse(isValid);
    }

    @Test
    void testIsTokenInvalidAfterExpiration() {
        JwtService spyService = spy(jwtService);
        doReturn(new Date(System.currentTimeMillis() + expiration + 1)).when(spyService).getCurrentDate();

        boolean isValid = spyService.isTokenValid(testToken, testUser);
        assertFalse(isValid);
    }
}

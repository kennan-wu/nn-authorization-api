package com.kennan.mp3player.mp3_player_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.kennan.mp3player.mp3_player_api.configs.JwtDecoderConfigTest;
import com.kennan.mp3player.mp3_player_api.model.User;

@SpringBootTest
@ContextConfiguration(classes = JwtDecoderConfigTest.class)
public class JwtServiceTest {
    @Autowired
    private JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;


    private String testToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        User user = User.builder()
            .email("test@example.com")
            .name("testUser")
            .password("password")
            .build();
        testUser = user;

        String token = jwtService.generateToken(user);
        testToken = token;
    }

    @Test
    void testGenerateToken() {
        assertNotNull(testToken);
    }

    @Test
    void testExtractClaim() {
        String username = jwtService.extractClaim(testToken, "email");
        assertEquals(username, testUser.getUsername());
    }

    @Test
    void testIsTokenValidWithSameUser() {
        boolean isValid = jwtService.isTokenValid(testToken, testUser);
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidAfterTime() {
        JwtService jwtServiceSpy = spy(jwtService);
        doReturn(new Date(System.currentTimeMillis() + expiration - 5000)).when(jwtServiceSpy).getCurrentDate();
        boolean isValid = jwtServiceSpy.isTokenValid(testToken, testUser);
        assertTrue(isValid);
    }

    @Test
    void testIsTokenInalidWithAnotherUser() {
        User anotherUser = User.builder()
            .email("another@example.com")
            .name("anotherUser")
            .password("anotherPassword")
            .build();
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

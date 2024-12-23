package com.kennan.mp3player.mp3_player_api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kennan.mp3player.mp3_player_api.dto.LoginUserDTO;
import com.kennan.mp3player.mp3_player_api.dto.RegisterUserDTO;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.responses.LoginResponse;
import com.kennan.mp3player.mp3_player_api.service.AuthenticationService;
import com.kennan.mp3player.mp3_player_api.service.JwtService;

@SpringBootTest
public class AuthenticationControllerTest {
    @Spy
    @Autowired
    private JwtService jwtServiceSpy;

    @Mock 
    private AuthenticationService authenticationServiceMock;

    private AuthenticationController authenticationController;
    private RegisterUserDTO registerUserDTO;
    private LoginUserDTO loginUserDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        authenticationController = new AuthenticationController(
            jwtServiceSpy, 
            authenticationServiceMock
        );
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setPassword("password");
        registerUserDTO.setUsername("testUser");

        loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("test@example.com");
        loginUserDTO.setPassword("password");

        testUser = new User("1", "testUser", "test@example.com", "password");
    }

    @Test
    void testSignUpUniqueUserReturnsOk() {
        when(authenticationServiceMock.signup(registerUserDTO)).thenReturn(testUser);

        ResponseEntity<User> response = authenticationController.register(registerUserDTO);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);

        verify(authenticationServiceMock, times(1)).signup(registerUserDTO);
    }

    @Test
    void testSignUpExistingUserThrowsException() {
        when(authenticationServiceMock.signup(registerUserDTO)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> {
            authenticationController.register(registerUserDTO);
        });
    }

    @Test
    void testLoginExistingUserReturnsOk() {
        when(authenticationServiceMock.authenticate(loginUserDTO)).thenReturn(testUser);

        ResponseEntity<LoginResponse> response = authenticationController.authenticate(loginUserDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("null")
        String token = response.getBody().getToken();
        assertNotNull(token);
        assertEquals("testUser", jwtServiceSpy.extractUsername(token));
    }

    @Test
    void testLoginNonExistingUserThrowException() {
        when(authenticationServiceMock.authenticate(loginUserDTO)).thenThrow(NoSuchElementException.class);

        assertThrows(NoSuchElementException.class, () -> {
            authenticationController.authenticate(loginUserDTO);
        });
    }
}

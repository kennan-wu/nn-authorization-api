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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.kennan.mp3player.mp3_player_api.dto.LoginUserDTO;
import com.kennan.mp3player.mp3_player_api.dto.RegisterUserDTO;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.service.AuthenticationService;
import com.kennan.mp3player.mp3_player_api.service.JwtService;
import com.kennan.mp3player.mp3_player_api.service.OAuthService;
import com.kennan.mp3player.mp3_player_api.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
public class AuthenticationControllerTest {
    @Mock
    private JwtService jwtServiceMock;

    @Mock
    private AuthenticationService authenticationServiceMock;

    @Mock
    private OAuthService oAuthServiceMock;

    @Mock
    private HttpServletResponse httpServletResponseMock;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserDetailsService userDetailsService;

    private AuthenticationController authenticationController;
    private RegisterUserDTO registerUserDTO;
    private LoginUserDTO loginUserDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        authenticationController = new AuthenticationController(
                jwtServiceMock,
                authenticationServiceMock,
                oAuthServiceMock,
                refreshTokenService,
                userDetailsService);
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setPassword("password");
        registerUserDTO.setUsername("testUser");

        loginUserDTO = new LoginUserDTO();
        loginUserDTO.setEmail("test@example.com");
        loginUserDTO.setPassword("password");

        testUser = User.builder()
                .email("test@example.com")
                .name("testUser")
                .password("password")
                .build();
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

        ResponseEntity<User> response = authenticationController.authenticate(loginUserDTO, httpServletResponseMock);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testLoginNonExistingUserThrowException() {
        when(authenticationServiceMock.authenticate(loginUserDTO)).thenThrow(NoSuchElementException.class);

        assertThrows(NoSuchElementException.class, () -> {
            authenticationController.authenticate(loginUserDTO, httpServletResponseMock);
        });
    }
}

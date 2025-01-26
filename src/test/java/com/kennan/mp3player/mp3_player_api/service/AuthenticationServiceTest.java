package com.kennan.mp3player.mp3_player_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kennan.mp3player.mp3_player_api.dto.LoginUserDTO;
import com.kennan.mp3player.mp3_player_api.dto.RegisterUserDTO;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.repository.UserRepository;

@SpringBootTest
public class AuthenticationServiceTest {
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private PasswordEncoder passwordEncoderMock;

    @Mock
    private AuthenticationManager authenticationManagerMock;

    @Mock
    private RefreshTokenService refreshTokenServiceMock;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        this.authenticationService = new AuthenticationService(userRepositoryMock, passwordEncoderMock,
                authenticationManagerMock, jwtService, refreshTokenServiceMock);
    }

    @Test
    void testSignUpUserAndSaveToDatabase() {
        RegisterUserDTO input = new RegisterUserDTO();
        input.setEmail("test@example.com");
        input.setUsername("testUser");
        input.setPassword("rawPassword");

        com.kennan.mp3player.mp3_player_api.model.User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setName("testUser");
        savedUser.setPassword("encodedPassword");

        when(passwordEncoderMock.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepositoryMock.save(any(User.class))).thenReturn(savedUser);

        User result = authenticationService.signup(input);

        assertNotNull(result);
        assertEquals(savedUser, result);

        verify(passwordEncoderMock, times(1)).encode("rawPassword");
        verify(userRepositoryMock, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticateSuccess() {
        LoginUserDTO input = new LoginUserDTO();
        input.setEmail("test@example.com");

        User returnedUser = new User();
        returnedUser.setEmail("test@example.com");
        returnedUser.setName("testUser");
        returnedUser.setPassword("encodedPassword");

        when(userRepositoryMock.findByEmail(input.getEmail())).thenReturn(Optional.of(returnedUser));

        User result = authenticationService.authenticate(input);

        assertNotNull(result);
        assertEquals(returnedUser, result);

        verify(authenticationManagerMock, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepositoryMock, times(1))
                .findByEmail("test@example.com");
    }

    @Test
    void testAuthenticateFailure() {
        LoginUserDTO input = new LoginUserDTO();
        input.setEmail("test@example.com");

        when(userRepositoryMock.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            authenticationService.authenticate(input);
        });

        verify(authenticationManagerMock, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepositoryMock, times(1))
                .findByEmail("test@example.com");
    }
}

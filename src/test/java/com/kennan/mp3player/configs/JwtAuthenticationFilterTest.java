package com.kennan.mp3player.configs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.kennan.mp3player.mp3_player_api.configs.JwtAuthenticationFilter;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.service.JwtService;

@SpringBootTest
public class JwtAuthenticationFilterTest {
    @Autowired
    private JwtService jwtService;

    @Mock
    private JwtService jwtServiceMock;

    @Mock
    private UserDetailsService userDetailsServiceMock;

    @Mock
    HandlerExceptionResolver handlerExceptionResolverMock;

    @BeforeEach
    void setUp() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtServiceMock, 
            userDetailsServiceMock, 
            handlerExceptionResolverMock
        );
    }

    @Test
    void testValidTokenSetsAuthentication() {
        // User testUser = new User("1", "testUser", "test@example.com", "password");
        // String token = jwtService.generateToken(testUser);
        
        when(jwtServiceMock.extractUsername(anyString())).thenReturn("testUser");
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
        
    }
}

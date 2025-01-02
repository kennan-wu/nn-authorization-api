package com.kennan.mp3player.mp3_player_api.configs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
public class JwtAuthenticationFilterTest {
    private JwtAuthenticationFilter filter;

    private User testUser;

    @Spy
    @Autowired
    private JwtService jwtServiceSpy;

    @Mock
    private UserDetailsService userDetailsServiceMock;

    @Mock
    HandlerExceptionResolver handlerExceptionResolverMock;

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    FilterChain filterChainMock;

    @Mock
    private SecurityContext securityContextMock;
    @Mock
    private Authentication authenticationMock;

    @BeforeEach
    void setUp() {
            filter = new JwtAuthenticationFilter(
            jwtServiceSpy, 
            userDetailsServiceMock, 
            handlerExceptionResolverMock
        );
        User user = User.builder()
            .email("test@example.com")
            .username("testUser")
            .password("password")
            .build();
        testUser = user;
        SecurityContextHolder.setContext(securityContextMock);
    }

    @Test
    void testMissingAuthorizationHeader() throws Exception {
        when(requestMock.getHeader(anyString())).thenReturn(null);

        assertDoesNotThrow(() -> filter.doFilterInternal(requestMock, responseMock, filterChainMock));
        verify(filterChainMock, times(1)).doFilter(requestMock, responseMock);
    }

    @Test
    void testValidTokenSetsAuthentication() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        String token = jwtServiceSpy.generateToken(testUser);
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(securityContextMock.getAuthentication()).thenReturn(null);
        when(userDetailsServiceMock.loadUserByUsername(anyString())).thenReturn(testUser);

        assertDoesNotThrow(() -> filter.doFilterInternal(requestMock, responseMock, filterChainMock));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testUser", authentication.getName());
    }

    @Test
    void testValidTokenAndExistingAuthenticationPassesFilter() throws Exception{
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticationMock);
        SecurityContextHolder.setContext(securityContext);

        String token = jwtServiceSpy.generateToken(testUser);
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + token);

        assertDoesNotThrow(() -> filter.doFilterInternal(requestMock, responseMock, filterChainMock));
        verify(filterChainMock, times(1)).doFilter(requestMock, responseMock);
    }
}

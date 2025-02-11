package com.kennan.mp3player.mp3_player_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kennan.mp3player.mp3_player_api.service.AuthenticationService;
import com.kennan.mp3player.mp3_player_api.service.CookieService;
import com.kennan.mp3player.mp3_player_api.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LogoutController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public LogoutController(AuthenticationService authenticationService, JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.terminateSession(request, response, true);

        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        String idToken = CookieService.getCookieValue(request, "id_token");
        try {
            final String email = jwtService.extractClaim(idToken, "email");
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(idToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid");
            }
            return ResponseEntity.ok(userDetails);
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid");
        }
    }
}

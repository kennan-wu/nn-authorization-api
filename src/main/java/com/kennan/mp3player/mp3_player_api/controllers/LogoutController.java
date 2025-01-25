package com.kennan.mp3player.mp3_player_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kennan.mp3player.mp3_player_api.service.AuthenticationService;
import com.kennan.mp3player.mp3_player_api.service.CookieService;
import com.kennan.mp3player.mp3_player_api.service.JwtService;
import com.kennan.mp3player.mp3_player_api.service.OAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LogoutController {
    private final AuthenticationService authenticationService;

    public LogoutController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.terminateSession(request, response, true);

        return ResponseEntity.ok("Logged out successfully");
    }
}

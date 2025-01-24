package com.kennan.mp3player.mp3_player_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kennan.mp3player.mp3_player_api.service.CookieService;
import com.kennan.mp3player.mp3_player_api.service.JwtService;
import com.kennan.mp3player.mp3_player_api.service.OAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LogoutController {
    private final JwtService jwtService;

    public LogoutController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String idToken = CookieService.getCookieValue(request, "id_token");

        jwtService.blacklistToken(idToken);

        CookieService.removeHttpOnlyCookie("id_token", response);
        CookieService.removeHttpOnlyCookie("refresh_token", response);

        return ResponseEntity.ok("Logged out successfully");
    }
}

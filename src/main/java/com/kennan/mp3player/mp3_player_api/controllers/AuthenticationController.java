package com.kennan.mp3player.mp3_player_api.controllers;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kennan.mp3player.mp3_player_api.dto.LoginUserDTO;
import com.kennan.mp3player.mp3_player_api.dto.RegisterUserDTO;
import com.kennan.mp3player.mp3_player_api.exceptions.ExistingEmailException;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.service.AuthenticationService;
import com.kennan.mp3player.mp3_player_api.service.CookieService;
import com.kennan.mp3player.mp3_player_api.service.JwtService;
import com.kennan.mp3player.mp3_player_api.service.OAuthService;
import com.kennan.mp3player.mp3_player_api.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final OAuthService oAuthService;
    private final RefreshTokenService refreshService;

    public AuthenticationController(
            JwtService jwtService,
            AuthenticationService authenticationService,
            OAuthService oAuthService,
            RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.oAuthService = oAuthService;
        this.refreshService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDTO registerUserDTO) {
        User registeredUser = authenticationService.signup(registerUserDTO);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> authenticate(@RequestBody LoginUserDTO loginUserDTO, HttpServletResponse response,
            HttpServletRequest request) {
        User authenticatedUser = authenticationService.authenticate(loginUserDTO);
        boolean withRefreshToken = request.getParameter("refresh") != null;

        String jwtToken = jwtService.generateToken(authenticatedUser);
        if (withRefreshToken) {
            String refreshToken = refreshService.generateToken(authenticatedUser);
            CookieService.setHttpOnlyCookie(refreshToken, "refresh_token", response);
        }
        CookieService.setHttpOnlyCookie(jwtToken, "id_token", response);

        return ResponseEntity.ok(authenticatedUser);
    }

    @GetMapping("/oauth/authorize")
    public ResponseEntity<?> redirectToOAuthProvider(HttpServletRequest request, HttpServletResponse response) {
        try {
            boolean withRefreshToken = request.getParameter("refresh") != null;
            String encodedState = oAuthService.generateState(request);
            CookieService.setHttpOnlyCookie(encodedState, "state", response);

            String oAuthUri = oAuthService.buildOAuthUri(encodedState, withRefreshToken);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(oAuthUri)).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OAuth authorization failed: " + e.getMessage());
        }
    }

    @GetMapping("/oauth/callback")
    public void handleOAuthCallback(@RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String redirect = oAuthService.validateState(state, request);
        if (redirect == null) {
            response.sendRedirect("/");
            return;
        }

        Map<String, String> tokensMap = oAuthService.retrieveOauthTokens(code, false);
        String idToken = tokensMap.get("id_token");
        if (tokensMap.containsKey("refresh_token")) {
            String refreshToken = tokensMap.get("refresh_token");
            CookieService.setHttpOnlyCookie(refreshToken, "refresh_token", response);
        }
        CookieService.setHttpOnlyCookie(idToken, "id_token", response);

        String email = jwtService.extractClaim(idToken, "email");
        String username = jwtService.extractClaim(idToken, "name");
        String issuer = jwtService.extractClaim(idToken, "iss");
        RegisterUserDTO input = new RegisterUserDTO(
                email,
                UUID.randomUUID().toString(),
                username,
                issuer);
        try {
            authenticationService.signup(input);
        } catch (ExistingEmailException exception) {
            System.out.println("User already exists: " + input.getEmail());
        } finally {
            response.sendRedirect(redirect);
        }
    }

    @PostMapping("/refresh")
    public void refreshIdToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieService.getCookieValue(request, "refresh_token");
        String idToken = CookieService.getCookieValue(request, "id_token");

        String newIdToken = refreshService.refreshIdToken(idToken, refreshToken);
        CookieService.setHttpOnlyCookie(newIdToken, "id_token", response);
    }
}

package com.kennan.mp3player.mp3_player_api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kennan.mp3player.mp3_player_api.dto.LoginUserDTO;
import com.kennan.mp3player.mp3_player_api.dto.RegisterUserDTO;
import com.kennan.mp3player.mp3_player_api.exceptions.ExistingEmailException;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public User signup(RegisterUserDTO input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new ExistingEmailException();
        }

        User user = User.builder()
                .email(input.getEmail())
                .name(input.getUsername())
                .password(passwordEncoder.encode(input.getPassword()))
                .provider(input.getProvider())
                .build();

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()));

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }

    public void terminateSession(HttpServletRequest request, HttpServletResponse response, boolean terminateRefresh) {
        String idToken = CookieService.getCookieValue(request, "id_token");

        jwtService.blacklistToken(idToken);
        if (terminateRefresh) {
            String refreshToken = CookieService.getCookieValue(request, "refresh_token");
            refreshTokenService.blacklistToken(refreshToken);
        }

        CookieService.removeHttpOnlyCookie("id_token", response);
        if (terminateRefresh) {
            CookieService.removeHttpOnlyCookie("refresh_token", response);
        }
    }

}

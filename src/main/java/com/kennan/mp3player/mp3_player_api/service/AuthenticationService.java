package com.kennan.mp3player.mp3_player_api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kennan.mp3player.mp3_player_api.dto.LoginUserDTO;
import com.kennan.mp3player.mp3_player_api.dto.RegisterUserDTO;
import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class AuthenticationService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    public AuthenticationService(
        UserRepository userRepository, 
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signup(RegisterUserDTO input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        User user = User.builder()
            .email(input.getEmail())
            .username(input.getUsername())
            .password(passwordEncoder.encode(input.getPassword()))
            .provider(input.getProvider())
            .build();

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDTO input) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.getEmail(),
                input.getPassword()
            )
        );

        return userRepository.findByEmail(input.getEmail())
            .orElseThrow();
    }
    
}

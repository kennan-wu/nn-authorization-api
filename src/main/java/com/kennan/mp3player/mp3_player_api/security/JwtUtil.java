package com.kennan.mp3player.mp3_player_api.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

@Component
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}

package com.kennan.mp3player.mp3_player_api.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Service
public class JwtService {
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Getter
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${issuer.name}")
    private String defaultIssuer;

    private final NimbusJwtDecoder googleJwtDecoder;
    private final NimbusJwtDecoder jwtDecoder;

    public JwtService(NimbusJwtDecoder googleJwtDecoder, NimbusJwtDecoder jwtDecoder) {
        this.googleJwtDecoder = googleJwtDecoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateToken(UserDetails userDetails) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer(defaultIssuer)
                .build();

            SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256), 
                claims
            );

            JWSSigner signer = new MACSigner(secretKey);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch(Exception exception) {
            throw new JwtException("Failed to generate JWT:" + exception.getMessage());
        }
    }

    private Jwt decodeToken(String token) {
        String issuer = extractIssuer(token);
        if ("https://accounts.google.com".equals(issuer)) {
            return googleJwtDecoder.decode(token);
        } else {
            return jwtDecoder.decode(token);
        }
    }

    private String extractIssuer(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
            return payload.contains("\"iss\":\"") ? payload.split("\"iss\":\"")[1].split("\"")[0] : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract issuer from token", e);
        }
    }

    public String extractClaim(String token, String claim) {
        Jwt jwt = decodeToken(token);
        return jwt.getClaim(claim).toString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Jwt jwt = decodeToken(token);
            String username = jwt.getClaim("sub");
            return username.equals(userDetails.getUsername());
        } catch(Exception exception) {
            return false;
        }
    }
}
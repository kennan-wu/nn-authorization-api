package com.kennan.mp3player.mp3_player_api.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.kennan.mp3player.mp3_player_api.model.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.JwtException;

@Service
public class RefreshTokenService {
    @Value("${issuer.name}")
    private String defaultIssuer;

    private final NimbusJwtDecoder refreshDecoder;

    public RefreshTokenService(NimbusJwtDecoder nimbusJwtDecoder) {
        this.refreshDecoder = nimbusJwtDecoder;
    }

    public String generateToken(User user) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .claim("type", "refresh")
                .claim("version", user.getRefreshVersion())
                .issueTime(new Date(System.currentTimeMillis()))
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
    // generate token
    // validate token
    // refresh jwt
    // blacklist token
}

package com.kennan.mp3player.mp3_player_api.service;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.repository.UserRepository;
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

    @Value("${refresh.secret.key}")
    private String refreshSecret;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${google.token.url}")
    private String tokenUrl;

    @Value("${google.token.revoke.url}")
    private String revokeUrl;

    private final NimbusJwtDecoder refreshDecoder;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public RefreshTokenService(NimbusJwtDecoder refreshDecoder, JwtService jwtService, RestTemplate restTemplate,
            UserRepository userRepository) {
        this.refreshDecoder = refreshDecoder;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    public String generateToken(User user) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .claim("type", "refresh")
                    .claim("version", user.getRefreshVersion())
                    .issueTime(new Date(System.currentTimeMillis()))
                    .issuer(defaultIssuer)
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims);

            JWSSigner signer = new MACSigner(refreshSecret);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception exception) {
            throw new JwtException("Failed to generate JWT:" + exception.getMessage());
        }
    }

    private Jwt decodeToken(String token) {
        return refreshDecoder.decode(token);
    }

    // validate token
    private boolean isTokenValid(String idToken, String refreshToken) {
        Jwt refresh = decodeToken(refreshToken);
        int refreshVersion = Math.toIntExact(refresh.getClaim("version"));

        String email = jwtService.extractClaim(idToken, "email");
        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getRefreshVersion() != refreshVersion) {
                throw new RuntimeException(("Expired refresh token"));
            }
            return true;
        } catch (RuntimeException e) {
            System.out.println("Refresh Token invalid: " + e.getMessage());
            return false;
        }
    }

    public String refreshIdToken(String idToken, String refreshToken) {
        String issuer = jwtService.extractIssuer(idToken);
        jwtService.blacklistToken(idToken);
        if ("https://accounts.google.com".equals(issuer)) {
            return refreshGoogleToken(refreshToken);
        } else if (isTokenValid(idToken, refreshToken)) {
            UserDetails user = User.builder().email(jwtService.extractClaim(idToken, "email")).build();
            return refreshCustomIdToken(user);
        } else {
            throw new RuntimeException("Invalid Refresh Token");
        }
    }

    private String refreshCustomIdToken(UserDetails user) {
        return jwtService.generateToken(user);
    }

    private String refreshGoogleToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("id_token")) {
                throw new IllegalArgumentException("Failed to obtain id_token from Google");
            }

            return (String) responseBody.get("id_token");

        } catch (Exception e) {
            throw new RuntimeException("Error refreshing Google token", e);
        }
    }

    public String blacklistToken(String idToken, String refreshToken) {
        String issuer = jwtService.extractIssuer(idToken);
        if ("https://accounts.google.com".equals(issuer)) {
            return blacklistGoogleToken(refreshToken);
        } else if (isTokenValid(idToken, refreshToken)) {
            UserDetails user = User.builder().email(jwtService.extractClaim(idToken, "email")).build();
            return blacklistCustomToken(user, idToken);
        } else {
            throw new RuntimeException("Invalid Refresh Token");
        }
    }

    private String blacklistCustomToken(UserDetails user, String token) {
        String email = user.getUsername();
        userRepository.incrementRefreshVersion(email);
        return token;
    }

    private String blacklistGoogleToken(String token) {
        String url = revokeUrl + "?token=" + token;
        restTemplate.getForObject(url, String.class);
        return token;
    }
}

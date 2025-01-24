package com.kennan.mp3player.mp3_player_api.configs;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtDecoderFactory {

    public NimbusJwtDecoder withJwkSetUri(String jwkSetUrl) {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUrl).build();
    }

    public NimbusJwtDecoder withSecretKey(String jwtSecret) {
        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}

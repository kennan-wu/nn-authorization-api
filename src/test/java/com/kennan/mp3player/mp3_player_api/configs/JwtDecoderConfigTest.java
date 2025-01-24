package com.kennan.mp3player.mp3_player_api.configs;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.kennan.mp3player.mp3_player_api.service.JwtService;

@TestConfiguration
public class JwtDecoderConfigTest {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    @Bean
    public NimbusJwtDecoder mockGoogleJwtDecoder() {
        return Mockito.mock(NimbusJwtDecoder.class);
    }

    @Bean
    public NimbusJwtDecoder testJwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtService jwtService(
        NimbusJwtDecoder mockGoogleJwtDecoder, 
        NimbusJwtDecoder jwtDecoder, 
        RedisTemplate<String, String> redisTemplate
    ) {
        return new JwtService(mockGoogleJwtDecoder, jwtDecoder, redisTemplate);
    }
}

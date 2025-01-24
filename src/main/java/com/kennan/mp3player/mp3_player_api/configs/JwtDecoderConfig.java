package com.kennan.mp3player.mp3_player_api.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    private final JwtDecoderFactory jwtDecoderFactory;

    public JwtDecoderConfig(JwtDecoderFactory jwtDecoderFactory) {
        this.jwtDecoderFactory = jwtDecoderFactory;
    }

    @Bean
    public NimbusJwtDecoder jwtDecoder() {
        return jwtDecoderFactory.withSecretKey(jwtSecret);
    }

    @Bean
    public NimbusJwtDecoder googleJwtDecoder() {
        return jwtDecoderFactory.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs");
    }

    // @Bean
    // public NimbusJwtDecoder githubJwtDecoder() {
    //     return jwtDecoderFactory.createDecoder("https://github.com/login/oauth/certs");
    // }
}

package com.kennan.mp3player.mp3_player_api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class OAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth.url}")
    private String oauthUri;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;

    @Value("${google.token.url}")
    private String tokenUrl;

    private final RestTemplate restTemplate;

    public OAuthService(
            RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String validateState(String encodedState, HttpServletRequest request) {
        String encodedCookieState = CookieService.getCookieValue(request, "state");

        if (encodedCookieState == null || !encodedCookieState.equals(encodedState)) {
            return null;
        }
        try {
            String decodedState = new String(Base64.getDecoder().decode(encodedState));
            return extractRedirectFromPayload(decodedState);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String extractRedirectFromPayload(String decodedPayload) {
        int start = decodedPayload.indexOf("\"redirectTo\":") + 15;
        int end = decodedPayload.length() - 2;
        return (start > 12 && end > start) ? decodedPayload.substring(start, end) : null;
    }

    public Map<String, String> retrieveOauthTokens(String grant, boolean isRefresh) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>() {
            {
                add("client_id", clientId);
                add("client_secret", clientSecret);
                add("grant_type", isRefresh ? "refresh_token" : "authorization_code");
                add(isRefresh ? "refresh_token" : "code", grant);
                if (!isRefresh)
                    add("redirect_uri", redirectUri);
            }
        };

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null)
            throw new IllegalStateException("Response body is null");

        return Map.of(
                "id_token", (String) responseBody.get("id_token"),
                "refresh_token", (String) responseBody.getOrDefault("refresh_token", ""));
    }

    public String generateState(HttpServletRequest request) {
        String redirectTo = request.getHeader("Referer");
        if (redirectTo == null) {
            throw new IllegalArgumentException("Referer header not defined");
        }
        String secureState = UUID.randomUUID().toString();
        String statePayload = "{\"state\": \"" + secureState + "\", \"redirectTo\": \"" + redirectTo + "\"}";
        String encodedState = Base64.getEncoder().encodeToString(statePayload.getBytes());
        return encodedState;
    }

    public String buildOAuthUri(String state, boolean withRefreshToken) {
        return oauthUri + "?" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&" +
                "redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&" +
                "response_type=code&" +
                "scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) + "&" +
                "state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) + "&" +
                "access_type=offline" +
                (withRefreshToken ? "&prompt=consent" : "");
    }
}

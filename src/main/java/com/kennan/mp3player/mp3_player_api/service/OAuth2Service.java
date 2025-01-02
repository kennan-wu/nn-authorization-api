package com.kennan.mp3player.mp3_player_api.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.kennan.mp3player.mp3_player_api.model.User;
import com.kennan.mp3player.mp3_player_api.repository.UserRepository;

@Service
public class OAuth2Service extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public OAuth2Service(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        System.out.println("Attributes: " + attributes);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getName();
        String oAuthId = oAuth2User.getAttribute("sub");
        
        if (oAuthId == null) {
            throw new IllegalArgumentException("OAuth2 response missing 'sub' attribute");
        }
        if(userRepository.existsByEmail(email)) {
            return oAuth2User;
        }

        User user = User
            .builder()
            .email(email)
            .username(name)
            .oauthProvider("GOOGLE")
            .oauthId(oAuthId)
            .build();
        userRepository.save(user);

        return oAuth2User;
    }
}

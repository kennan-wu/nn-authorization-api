package com.kennan.mp3player.mp3_player_api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private long expiresIn;
}

package com.kennan.mp3player.mp3_player_api.dto;

import com.kennan.mp3player.mp3_player_api.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDTO {
    private String email;
    private String password;
    private String username;
}

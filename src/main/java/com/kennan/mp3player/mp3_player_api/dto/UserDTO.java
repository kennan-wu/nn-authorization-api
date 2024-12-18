package com.kennan.mp3player.mp3_player_api.dto;

import com.kennan.mp3player.mp3_player_api.model.User;

import lombok.Getter;

@Getter
public class UserDTO {
    private String id;
    private String username;
    private String email;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}

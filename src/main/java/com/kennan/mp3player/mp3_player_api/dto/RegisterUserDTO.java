package com.kennan.mp3player.mp3_player_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDTO {
    private String email;
    private String password;
    private String username;
    private String provider = "neural-lab.com";
}

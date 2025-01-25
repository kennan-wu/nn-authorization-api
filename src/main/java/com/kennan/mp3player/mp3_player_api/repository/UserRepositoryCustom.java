package com.kennan.mp3player.mp3_player_api.repository;

public interface UserRepositoryCustom {
    int incrementRefreshVersion(String email);
}

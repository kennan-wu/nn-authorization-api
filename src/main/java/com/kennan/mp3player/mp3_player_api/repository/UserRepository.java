package com.kennan.mp3player.mp3_player_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.kennan.mp3player.mp3_player_api.model.User;

public interface UserRepository extends MongoRepository<User, String>{
    boolean existsByEmail(String email);
    User findByUsername(String username);
}

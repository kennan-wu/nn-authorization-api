package com.kennan.mp3player.mp3_player_api.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.kennan.mp3player.mp3_player_api.model.User;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}

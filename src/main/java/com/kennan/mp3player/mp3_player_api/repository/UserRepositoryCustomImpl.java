package com.kennan.mp3player.mp3_player_api.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.kennan.mp3player.mp3_player_api.model.User;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public int incrementRefreshVersion(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        Update update = new Update().inc("refreshVersion", 1);

        // Perform findAndModify
        User user = mongoTemplate.findAndModify(
                query,
                update,
                User.class);

        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        return user.getRefreshVersion();
    }
}

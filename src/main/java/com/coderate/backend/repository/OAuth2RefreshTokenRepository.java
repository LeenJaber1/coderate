package com.coderate.backend.repository;

import com.coderate.backend.model.OAuth2Refresh;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OAuth2RefreshTokenRepository extends MongoRepository<OAuth2Refresh, String> {
    Optional<OAuth2Refresh> findByUserIdAndRegistrationId(String userId, String registrationId);

    void deleteByUserIdAndRegistrationId(String userId, String registrationId);
}

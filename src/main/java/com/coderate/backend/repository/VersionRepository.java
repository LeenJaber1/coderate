package com.coderate.backend.repository;

import com.coderate.backend.model.Version;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VersionRepository extends MongoRepository<Version, String> {
    List<Version> findByProjectIdAndUserId(String projectId , String userId);
    Optional<Version> findByProjectIdAndVersionNumber(String projectId, int versionNumber);
    void deleteAllByProjectId(String projectId);
}

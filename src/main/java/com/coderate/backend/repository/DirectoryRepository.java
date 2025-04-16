package com.coderate.backend.repository;

import com.coderate.backend.model.Directory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DirectoryRepository extends MongoRepository<Directory, String> {
    @Query("{ 'project.$id' : ?0 }")
    List<Directory> findByProjectId(String projectId);
    @Query("{ 'user.$id' : ?0 }")
    List<Directory> findByUserId(String userId);
}

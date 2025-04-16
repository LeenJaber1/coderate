package com.coderate.backend.repository;

import com.coderate.backend.model.File;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends MongoRepository<File , String> {
    @Query("{ 'user.$id' : ?0 }")
    List<File> findByUserId(String userId);
    List<File> findByVersion(int version);
    @Query("{ 'project.$id' : ?0 }")
    List<File> findByProjectId(String projectId);
    @Query("{ 'parentDirectory.$id' : ?0 }")
    List<File> findByDirectoryId(String directoryId);
    @Query("{ 'project.$id': ?0, 'version': ?1 }")
    List<File> findByProjectAndVersion(String projectId, int version);
    @Query("{ 'parentDirectory.$id': ?0, 'version': ?1 }")
    List<File> findByDirectoryAndVersion(String parentDirectoryId, int version);
    Optional<File> findByFilePathAndVersionAndProject_Id(String filePath, int version, String projectId);

}

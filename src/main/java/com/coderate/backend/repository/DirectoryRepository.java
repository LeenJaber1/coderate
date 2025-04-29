package com.coderate.backend.repository;

import com.coderate.backend.model.Directory;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DirectoryRepository extends MongoRepository<Directory, String> {
    List<Directory> findByVersionNumberAndProjectId(int version, String projectId);

    List<Directory> findByUserIdAndProjectId(String userId, String projectId);

    List<Directory> findByProjectId(String projectId);

    List<Directory> findByParentDirectoryId(String parentDirectoryId);

    List<Directory> findByVersionId(String versionId);


    @Aggregation(pipeline = {
            // 1. Match directories in a specific project AND with versionNumber <= the input version
            "{ '$match': { 'projectId': ?0, 'versionNumber': { '$lte': ?1 } } }",

            // 2. Sort by path (asc) and versionNumber (desc) to ensure highest version comes first
            "{ '$sort': { 'path': 1, 'versionNumber': -1 } }",

            // 3. Group by path (within the project) and keep the first document (max version)
            "{ '$group': { '_id': '$path', 'directoryWithMaxVersion': { '$first': '$$ROOT' } } }",

            // 4. Replace root to return the full directory document
            "{ '$replaceRoot': { 'newRoot': '$directoryWithMaxVersion' } }"
    })
    List<Directory> findLatestVersionDirectoriesInProject(String projectId, Integer version);

    List<Directory> findByProjectIdAndVersionNumber(String projectId, Integer versionNumber);

    @Aggregation(pipeline = {
            "{ '$match': { 'projectId': ?0, 'versionNumber': { '$lte': ?1 }, 'path': ?2 } }",  // Match by projectId, versionNumber less than the given one, and specific path
            "{ '$sort': { 'versionNumber': -1 } }",  // Sort by versionNumber in descending order
            "{ '$limit': 1 }"  // Limit to only the top result
    })
    Optional<Directory> findTopDirectoryInProjectByPath(String projectId, Integer versionNumber, String path);


    void deleteAllByProjectId(String projectId);
}

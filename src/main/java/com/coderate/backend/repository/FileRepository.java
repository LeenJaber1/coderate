package com.coderate.backend.repository;

import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends MongoRepository<File , String> {
    List<File> findByVersionNumberAndProjectId(int version, String projectId);

    List<File> findByUserIdAndProjectId(String userId, String projectId);

    List<File> findByProjectId(String projectId);

    List<File> findByParentDirectoryId(String parentDirectoryId);

    List<File> findByVersionId(String versionId);

    @Aggregation(pipeline = {
            // 1. Match files in a specific project AND with versionNumber <= the given value
            "{ '$match': { 'projectId': ?0, 'versionNumber': { '$lte': ?1 } } }",

            // 2. Sort by path (asc) and versionNumber (desc) to get highest version per path first
            "{ '$sort': { 'path': 1, 'versionNumber': -1 } }",

            // 3. Group by path to get the top file per path
            "{ '$group': { '_id': '$path', 'fileWithMaxVersion': { '$first': '$$ROOT' } } }",

            // 4. Replace the root with the actual file document
            "{ '$replaceRoot': { 'newRoot': '$fileWithMaxVersion' } }"
    })
    List<File> findLatestVersionFilesInProject(String projectId, Integer maxVersionNumber);

    Optional<File> findByPathAndProjectIdAndVersionNumber(String path , String projectId, int versionNumber);

    List<File> findByProjectIdAndVersionNumber(String projectId, Integer versionNumber);

    @Aggregation(pipeline = {
            "{ '$match': { 'projectId': ?0, 'versionNumber': { '$lte': ?1 }, 'path': ?2 } }",  // Match by projectId, versionNumber less than the given one, and specific path
            "{ '$sort': { 'versionNumber': -1 } }",  // Sort by versionNumber in descending order
            "{ '$limit': 1 }"  // Limit to only the top result
    })
    Optional<File> findTopFileInProjectByPath(String projectId, Integer versionNumber, String path);

    void deleteAllByProjectId(String projectId);
}

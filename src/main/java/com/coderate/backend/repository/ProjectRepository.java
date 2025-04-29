package com.coderate.backend.repository;

import com.coderate.backend.model.File;
import com.coderate.backend.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project , String> {
    @Query("{ 'owner.$id' : ?0 }")
    List<Project> findByUserId(String userId);

    @Query("{ 'usersRoles.?0' : { $exists: true } }")
    List<Project> findByUserIdInUsersRoles(String userId);
}

package com.coderate.backend.service;

import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.exceptions.NotAuthorizedException;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.exceptions.VersionNotFoundException;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import com.coderate.backend.response.ConflictResponse;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public interface ProjectService {
    boolean canEnterSessions(String projectId , String userId) throws ProjectNotFoundException;
    Project createProject(String projectName, ProgramLanguage language, User owner) throws DirectoryNotFoundException;

    void changeProjectName(String newName , String projectId, User owner) throws ProjectNotFoundException, NotAuthorizedException;

    Project getProjectById(String projectId) throws ProjectNotFoundException;

    // add if admin or owner
    void addUserRole(String userId , Role role, User ownerAdmin, Project project) throws NotAuthorizedException;

    User getProjectOwner(String projectId) throws ProjectNotFoundException;

    //delete if owner
    void deleteProject(String projectId, User user) throws ProjectNotFoundException, NotAuthorizedException;

    //remove if owner or admin
    void removeUserRole(String user, User ownerAdmin, Project project) throws NotAuthorizedException;

    ProjectStructure getVersion(int version , String projectI) throws ProjectNotFoundException, VersionNotFoundException;

    ProjectStructure changeVersion(int versionNumber, String projectId, String userId) throws ProjectNotFoundException, VersionNotFoundException;

    //if owner
    ProjectStructure changeProjectVersion(int version , String projectId, String userId) throws VersionNotFoundException, ProjectNotFoundException, NotAuthorizedException;

    ConflictResponse saveVersion(String userId, String projectId , String message , ProjectStructure projectStructureRequest) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException, DirectoryNotFoundException, FileAlreadyExistsException;

    Project forkProject(String projectId, User user) throws ProjectNotFoundException, FileAlreadyExistsException, DirectoryNotFoundException;

    Project cloneProject(String projectId , User user) throws ProjectNotFoundException, FileAlreadyExistsException, DirectoryNotFoundException;

    List<Project> getProjectsByUserId(String userId);
}
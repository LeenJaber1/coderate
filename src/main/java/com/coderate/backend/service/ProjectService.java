package com.coderate.backend.service;

import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import com.coderate.backend.exceptions.NotAuthorizedException;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ProjectService {
    Project createProject(String projectName, ProgramLanguage language, User owner);
    void changeProjectName(String newName , String projectId, User owner) throws ProjectNotFoundException, NotAuthorizedException;
    Project getProjectById(String projectId) throws ProjectNotFoundException;
    //use file service to get all the files for this project with certain version
    Project changeProjectVersion(int version, String projectId) throws ProjectNotFoundException;
    // add if admin or owner
    void addUserRole(User user , Role role, User ownerAdmin, Project project) throws NotAuthorizedException;
    void addNewFileToDirectory(String projectId, File file, Directory directory);
    void addNewDirectory(String projectId, Directory directory , Directory parentDirectory);
    List<UserDetails> getUsersByRole(Project project, Role role);
    User getProjectOwner(String projectId) throws ProjectNotFoundException;
    //delete if owner
    void deleteProject(String projectId, User user) throws ProjectNotFoundException, NotAuthorizedException;
    //remove if owner or admin
    void removeUserRole(User user, User ownerAdmin, Project project) throws NotAuthorizedException;
    Project forkProject(String projectId, User user) throws ProjectNotFoundException;
    Project cloneProject(String projectId , User user) throws ProjectNotFoundException;
}
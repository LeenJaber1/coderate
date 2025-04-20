package com.coderate.backend.controller;

import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.exceptions.NotAuthorizedException;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.exceptions.VersionNotFoundException;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import com.coderate.backend.service.ProjectService;
import com.coderate.backend.service.StorageStructureService;
import com.coderate.backend.service.VersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private ProjectService projectService;
    private StorageStructureService storageStructureService;
    private VersionService versionService;

    public ProjectController(ProjectService projectService, StorageStructureService storageStructureService, VersionService versionService) {
        this.projectService = projectService;
        this.storageStructureService = storageStructureService;
        this.versionService = versionService;
    }

    @PostMapping("/create-project")
    public ResponseEntity<?> createProject(@AuthenticationPrincipal User user, @RequestParam ProgramLanguage language, @RequestParam String projectName) throws DirectoryNotFoundException {
        Project project = this.projectService.createProject(projectName, language, user);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/change-name")
    public ResponseEntity<?> changeProjectName(@AuthenticationPrincipal User user , @RequestParam String newName , @RequestParam String projectId) throws ProjectNotFoundException, NotAuthorizedException {
        this.projectService.changeProjectName(newName, projectId, user);
        return ResponseEntity.ok(200);
    }

    @PostMapping("/delete-project")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal User user , @RequestParam String projectId) throws ProjectNotFoundException, NotAuthorizedException {
        this.projectService.deleteProject(projectId, user);
        return ResponseEntity.ok(200);
    }

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@AuthenticationPrincipal User user, @RequestParam String userId, @RequestParam Role role, @RequestParam String projectId) throws ProjectNotFoundException, NotAuthorizedException {
        Project project = this.projectService.getProjectById(projectId);
        this.projectService.addUserRole(userId, role ,user ,project);
        return ResponseEntity.ok(200);
    }


    @PostMapping("/remove-user")
    public ResponseEntity<?> removeUser(@AuthenticationPrincipal User user, @RequestParam String userId,  @RequestParam String projectId) throws ProjectNotFoundException, NotAuthorizedException {
        Project project = this.projectService.getProjectById(projectId);
        this.projectService.removeUserRole(userId, user , project);
        return ResponseEntity.ok(200);
    }

    @PostMapping("/change-user-version")
    public ResponseEntity<ProjectStructure> changeUserVersion(@AuthenticationPrincipal User user , @RequestParam int version , @RequestParam String projectId) throws ProjectNotFoundException, VersionNotFoundException {
        ProjectStructure projectStructure = this.projectService.changeVersion(version , projectId, user.getId());
        return ResponseEntity.ok(projectStructure);
    }

    @PostMapping("/change-project-version")
    public ResponseEntity<ProjectStructure> changeProjectVersion(@AuthenticationPrincipal User user , @RequestParam int version , @RequestParam String projectId) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException {
        ProjectStructure projectStructure = this.projectService.changeProjectVersion(version , user.getId(), projectId);
        return ResponseEntity.ok(projectStructure);
    }


    @PostMapping("/save-version")
    public ResponseEntity<?> saveVersion(@AuthenticationPrincipal User user, @RequestParam String projectId, @RequestParam String message, @RequestBody ProjectStructure projectStructure) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException {
        this.projectService.saveVersion(user.getId() , projectId , message, projectStructure);
        return ResponseEntity.ok(200);
    }

}

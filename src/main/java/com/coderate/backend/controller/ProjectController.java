package com.coderate.backend.controller;

import com.coderate.backend.dto.MessageEdit;
import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.MessageType;
import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.exceptions.NotAuthorizedException;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.exceptions.VersionNotFoundException;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import com.coderate.backend.response.ConflictResponse;
import com.coderate.backend.service.ProjectService;
import com.coderate.backend.util.CookieCreation;
import com.coderate.backend.websocket.ProjectSessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private ProjectService projectService;
    private ProjectSessionManager projectSessionManager;
    private SimpMessagingTemplate messagingTemplate;

    public ProjectController(ProjectService projectService, ProjectSessionManager projectSessionManager, SimpMessagingTemplate messagingTemplate) {
        this.projectService = projectService;

        this.projectSessionManager = projectSessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getProjects(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(this.projectService.getProjectsByUserId(user.getId()));
    }

    @PostMapping("/create-project")
    public ResponseEntity<Project> createProject(@AuthenticationPrincipal User user, @RequestParam ProgramLanguage language, @RequestParam String projectName) throws DirectoryNotFoundException, ProjectNotFoundException, VersionNotFoundException {
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
    public ResponseEntity<ProjectStructure> changeUserVersion(@AuthenticationPrincipal User user , @RequestParam int version , @RequestParam String projectId) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException {
        ProjectStructure projectStructure = this.projectSessionManager.getProjectStructure(projectId, projectId+'@'+version);
        if(projectStructure == null){
            projectStructure = this.projectService.changeVersion(version, projectId , user.getId());
        }
        return ResponseEntity.ok(projectStructure);
    }

    @PostMapping("/change-project-version")
    public ResponseEntity<ProjectStructure> changeProjectVersion(@AuthenticationPrincipal User user , @RequestParam int version , @RequestParam String projectId) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException {
        ProjectStructure projectStructure = this.projectService.changeProjectVersion(version , projectId ,user.getId());
        return ResponseEntity.ok(projectStructure);
    }


    @PostMapping("/save-version")
    public ResponseEntity<?> saveVersion(@AuthenticationPrincipal User user, @RequestParam String projectId, @RequestParam String message, @RequestBody ProjectStructure projectStructure) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException, FileAlreadyExistsException, DirectoryNotFoundException {
        ConflictResponse conflictResponse = this.projectService.saveVersion(user.getId() , projectId , message, projectStructure);
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId,
                new MessageEdit(MessageType.NEW_VERSION, user.getUsername(), System.currentTimeMillis(), ""));
        return ResponseEntity.ok(conflictResponse);
    }


    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestParam String projectId, @RequestParam String sessionId, @RequestBody ProjectStructure structure) {
        this.projectSessionManager.updateProjectStructure(projectId, sessionId, structure);
        // possibly store or trigger some action (like compiling, etc.)

        return ResponseEntity.ok("Run triggered");
    }
}

package com.coderate.backend.controller;

import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.exceptions.NotAuthorizedException;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import com.coderate.backend.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create-project")
    public ResponseEntity<?> createProject(@AuthenticationPrincipal User user, @RequestParam ProgramLanguage language, @RequestParam String projectName){
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

}

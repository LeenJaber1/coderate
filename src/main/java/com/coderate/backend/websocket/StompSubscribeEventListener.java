package com.coderate.backend.websocket;
import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.exceptions.VersionNotFoundException;
import com.coderate.backend.model.User;
import com.coderate.backend.service.ProjectService;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;


@Component
public class StompSubscribeEventListener implements ApplicationListener<SessionSubscribeEvent>  {
    private ProjectService projectService;
    private ProjectSessionManager projectSessionManager;

    public StompSubscribeEventListener(ProjectService projectService, ProjectSessionManager projectSessionManager) {
        this.projectService = projectService;
        this.projectSessionManager = projectSessionManager;
    }

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();
        User user = ((User) ((UsernamePasswordAuthenticationToken) event.getUser()).getPrincipal());
        String[] arr = destination.split("/");
        String projectId = arr[arr.length - 1].split("@")[0];
        int version = Integer.parseInt(arr[arr.length - 1].split("@")[1]);
        String sessionId = arr[arr.length - 1];
        try {
            ProjectStructure projectStructure = this.projectService.changeVersion(version , projectId, user.getId());
            this.projectSessionManager.registerSession(sessionId , projectId ,version , projectStructure);
        } catch (ProjectNotFoundException e) {
            throw new RuntimeException(e);
        } catch (VersionNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
package com.coderate.backend.websocket;

import com.coderate.backend.model.User;
import com.coderate.backend.service.ProjectService;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class StompUnsunscribeEventListener implements ApplicationListener<SessionUnsubscribeEvent> {
    private ProjectService projectService;
    private ProjectSessionManager projectSessionManager;

    public StompUnsunscribeEventListener(ProjectService projectService, ProjectSessionManager projectSessionManager) {
        this.projectService = projectService;
        this.projectSessionManager = projectSessionManager;
    }

    @Override
    public void onApplicationEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getFirstNativeHeader("destination"); // ✅ Works if sent manually
        if (destination == null) return;

        User user = ((User) ((UsernamePasswordAuthenticationToken) event.getUser()).getPrincipal());
        String[] arr = destination.split("/");
        String projectId = arr[arr.length - 1].split("@")[0];
        int version = Integer.parseInt(arr[arr.length - 1].split("@")[1]);
        String sessionId = arr[arr.length - 1];
        this.projectSessionManager.unregisterUserFromSession(sessionId);
    }
}

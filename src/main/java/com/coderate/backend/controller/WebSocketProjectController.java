package com.coderate.backend.controller;

import com.coderate.backend.dto.MessageEdit;
import com.coderate.backend.exceptions.MissingSessionIdException;
import com.coderate.backend.model.User;
import com.coderate.backend.websocket.ProjectSessionManager;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class WebSocketProjectController {

    private final ProjectSessionManager sessionManager;

    public WebSocketProjectController(ProjectSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @MessageMapping("/edit/{sessionId}")
    public void handleEdit(@Header("simpSessionAttributes") Map<String, Object> attributes, @Payload MessageEdit message, @DestinationVariable String sessionId, @AuthenticationPrincipal User user) throws MissingSessionIdException {
        String projectId = sessionId.split("@")[0];
        sessionManager.addEdit(message, sessionId, projectId);
    }
}

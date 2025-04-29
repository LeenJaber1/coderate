package com.coderate.backend.websocket;

import com.coderate.backend.dto.MessageEdit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class Broadcaster {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcast(MessageEdit edit, String sessionId) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, edit);
    }
}

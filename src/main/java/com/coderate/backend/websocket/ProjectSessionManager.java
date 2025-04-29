package com.coderate.backend.websocket;

import com.coderate.backend.dto.MessageEdit;
import com.coderate.backend.dto.ProjectStructure;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectSessionManager {
    // projectId -> List<Session> -> session for every version
    private final Map<String , List<Session>> activeSessions = new  HashMap<>();
    private final Map<String , String> sessionProject = new HashMap<>();
    private final Broadcaster broadcaster;

    public ProjectSessionManager(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public String getSessionId(String projectId, int version) {
        List<Session> sessions = activeSessions.get(projectId);
        if (sessions == null) {
            return null;
        }
        for (Session session : sessions) {
            if (session.getVersionNumber() == version) {
                new Thread(session).start();
                return session.getSessionId();
            }
        }
        return null;
    }


    public synchronized void registerSession(String sessionId, String projectId, Integer versionNumber, ProjectStructure projectStructure) {
        // Store session mapping
        if(sessionProject.containsKey(sessionId)){
            return;
        }
        Session session = new Session(sessionId , versionNumber, broadcaster);
        session.setProjectStructure(projectStructure);
        sessionProject.put(sessionId, projectId);
        activeSessions.compute(projectId, (key, existingList) -> {
            if (existingList == null) {
                existingList = new ArrayList<>();
            }
            existingList.add(session);
            return existingList;
        });

        new Thread(session).start();
    }

    public synchronized void unregisterUserFromSession(String sessionId){
        String projectId = this.sessionProject.get(sessionId);
        for(Session session : activeSessions.get(projectId)){
            if(session.getSessionId().equals(sessionId) && session.isRunning()){
               session.setCount(session.getCount() - 1);
               if(session.getCount() == 0){
                   activeSessions.get(projectId).remove(session);
                   sessionProject.remove(sessionId);
               }
            }
        }

    }

    public ProjectStructure getProjectStructure(String projectId ,String sessionId) {
        ProjectStructure projectStructure = null;
        for(Session session : activeSessions.get(projectId)){
            if(session.getSessionId().equals(sessionId)){
                projectStructure = session.getProjectStructure();
            }
        }
        return projectStructure;
    }


    public void updateProjectStructure(String projectId, String sessionId, ProjectStructure updated) {
        for(Session session : activeSessions.get(projectId)){
            if(session.getSessionId().equals(sessionId)){
                session.setProjectStructure(updated);
            }
        }
    }

    public void addEdit(MessageEdit messageEdit , String sessionId, String projectId){
        for(Session session : activeSessions.get(projectId)){
            if(session.getSessionId().equals(sessionId)){
                session.getQueue().add(messageEdit);
            }
        }
    }

    public Map<String, List<Session>> getActiveSessions() {
        return this.activeSessions;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }
}

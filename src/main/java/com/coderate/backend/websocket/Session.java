package com.coderate.backend.websocket;

import com.coderate.backend.dto.MessageEdit;
import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.MessageType;
import com.coderate.backend.service.StorageStructureService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class Session implements Runnable{
    private PriorityBlockingQueue<MessageEdit> queue;
    private String sessionId;
    private Integer versionNumber;
    private ProjectStructure projectStructure;
    private boolean running;
    private Broadcaster broadcaster;
    private  int count;

    public Session(PriorityBlockingQueue<MessageEdit> queue, String sessionId, Integer versionNumber, Broadcaster broadcaster) {
        this.queue = queue;
        this.sessionId = sessionId;
        this.versionNumber = versionNumber;
        this.broadcaster = broadcaster;
        this.projectStructure = new ProjectStructure();
        this.running = true;
        this.count = 1;
    }

    public Session(String sessionId, Integer versionNumber, Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.queue = new PriorityBlockingQueue<>();
        this.projectStructure = new ProjectStructure();
        this.running = true;
        this.sessionId = sessionId;
        this.versionNumber = versionNumber;
        this.count = 1;
    }

    public Session(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.running = true;
        this.queue = new PriorityBlockingQueue<>();
        this.projectStructure = new ProjectStructure();
        this.count = 1;
    }

    public int getCount() {
        return count;
    }



    public synchronized void setCount(int count) {
        this.count = count;
    }

    @Override
    public void run() {
        try {
            while (true){
                checkIfUsersConnected();
                MessageEdit messageEdit = this.queue.take();
                applyEdit(messageEdit);
                broadcaster.broadcast(messageEdit , sessionId);
            }
        } catch (InterruptedException e) {
            this.running = false;
            Thread.currentThread().interrupt();
        }
    }

    private void checkIfUsersConnected() {
        if(this.count == 0){
            Thread.currentThread().interrupt();
        }
    }

    private void applyEdit(MessageEdit messageEdit) {
        if(messageEdit.getMessageType() == MessageType.SESSION_DISCONNECT){
            Thread.currentThread().interrupt();
        } else if(messageEdit.getMessageType() == MessageType.ADD || messageEdit.getMessageType() == MessageType.EDIT || messageEdit.getMessageType() == MessageType.DELETE){
            String line = this.projectStructure.getFilesContents().get(messageEdit.getFilePath()).get(messageEdit.getLineIndex());
            String newLine = applyChangeOnLine(line , messageEdit.getCharIndex(),messageEdit.getMessageType(),
                    messageEdit.getCharacter());
            this.projectStructure.getFilesContents().get(messageEdit.getFilePath()).set(messageEdit.getLineIndex(), newLine);
        }
    }

    public String applyChangeOnLine(String line, int index, MessageType messageType, Character character) {
        if (line == null) line = "";
        if(messageType == MessageType.ADD){
            if (index >= line.length()) {
                // Append character at the end
                line = line + character;
            } else {
                // Insert character at index
                line = line.substring(0, index) + character + line.substring(index);
            }
        }

        else if (messageType == MessageType.EDIT) {
            if (index < 0 || index >= line.length()) return line;
            line =  line.substring(0, index) + character + line.substring(index + 1);
        }

        else if (messageType == MessageType.DELETE) {
            if (index < 0 || index >= line.length()) return line;
            line = line.substring(0, index) + line.substring(index + 1);
        }

        return  line;
    }

    public PriorityBlockingQueue<MessageEdit> getQueue() {
        return this.queue;
    }

    public void setQueue(PriorityBlockingQueue<MessageEdit> queue) {
        this.queue = queue;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }



    public Integer getVersionNumber() {
        return this.versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public ProjectStructure getProjectStructure() {
        return this.projectStructure;
    }

    public void setProjectStructure(ProjectStructure projectStructure) {
        this.projectStructure = projectStructure;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }

    public void setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }
}

package com.coderate.backend.model;

import com.coderate.backend.enums.State;
import org.springframework.data.annotation.Id;

public abstract class AbstractStorageStructure {
    @Id
    private String id;
    private int versionNumber;
    private String versionId;
    private String path;
    private String userId;
    private String parentDirectoryId;
    private State state;
    private String projectId;

    public AbstractStorageStructure(int versionNumber, String versionId, String path, String userId, String parentDirectoryId, State state, String projectId) {
        this.versionNumber = versionNumber;
        this.versionId = versionId;
        this.path = path;
        this.userId = userId;
        this.state = state;
        this.parentDirectoryId = parentDirectoryId;
        this.projectId = projectId;
    }

    public AbstractStorageStructure() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return versionNumber;
    }

    public void setVersion(int version) {
        this.versionNumber = version;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParentDirectoryId() {
        return parentDirectoryId;
    }

    public void setParentDirectoryId(String parentDirectoryId) {
        this.parentDirectoryId = parentDirectoryId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}

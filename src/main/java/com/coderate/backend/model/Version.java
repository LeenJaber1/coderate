package com.coderate.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "version")
public class Version {
    @Id
    private String id;
    private String projectId;
    @DBRef
    private Version prevVersion;
    private List<String> editors;
    private String userId;
    private String message;
    private boolean isRoot;
    private int versionNumber;


    //prevVersion is the version the user is editing in , not the latest version the project is on
    public Version(Version prevVersion, String userId, String projectId, int versionNumber) {
        this.prevVersion = prevVersion;
        this.userId = userId;
        this.projectId = projectId;
        this.versionNumber = versionNumber;
        this.editors = new ArrayList<>();
        this.editors.add(userId);
    }

    public Version() {
        this.editors = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Version getPrevVersion() {
        return prevVersion;
    }

    public void setPrevVersion(Version prevVersion) {
        this.prevVersion = prevVersion;
    }


    public List<String> getEditors() {
        return editors;
    }

    public void setEditors(List<String> editors) {
        this.editors = editors;
    }
}

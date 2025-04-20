package com.coderate.backend.model;

import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "projects")
public class Project {
    @Id
    private String id;
    private String projectName;
    @DBRef
    private User owner;
    private Map<String , Role> usersRoles;
    private ProgramLanguage language;
    @DBRef
    private Version currentVersion;
    private Map<String , Integer> versionUserOn;
    private int latestVersion;
    @DBRef
    private Project forkedFrom;
    private String mainDirectoryId;

    public Project(String projectName, ProgramLanguage language, User owner) {
        this.projectName = projectName;
        this.language = language;
        this.owner = owner;
        this.usersRoles = new HashMap<>();
        this.versionUserOn = new HashMap<>();
        this.usersRoles.put(owner.getId() , Role.OWNER);
        this.latestVersion = 0;
    }

    public Project(User owner) {
        this.owner = owner;
        this.usersRoles = new HashMap<>();
        this.usersRoles.put(owner.getId() , Role.OWNER);
        this.versionUserOn = new HashMap<>();
    }

    public Project() {
        this.usersRoles = new HashMap<>();
        this.versionUserOn = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, Role> getUsersRoles() {
        return usersRoles;
    }

    public void setUsersRoles(Map<String, Role> usersRoles) {
        this.usersRoles = usersRoles;
    }


    public ProgramLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ProgramLanguage language) {
        this.language = language;
    }


    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Project getForkedFrom() {
        return forkedFrom;
    }

    public void setForkedFrom(Project forkedFrom) {
        this.forkedFrom = forkedFrom;
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Version currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Map<String, Integer> getVersionUserOn() {
        return versionUserOn;
    }

    public void setVersionUserOn(Map<String, Integer> versionUserOn) {
        this.versionUserOn = versionUserOn;
    }


    public int getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(int latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getMainDirectoryId() {
        return mainDirectoryId;
    }

    public void setMainDirectoryId(String mainDirectoryId) {
        this.mainDirectoryId = mainDirectoryId;
    }
}

package com.coderate.backend.model;

import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "projects")
public class Project {
    @Id
    private String id;
    private String projectName;
    // user id as key
    // set and add users after project creation
    @DBRef
    private User owner;
    private Map<String , Role> usersRoles;
    @DBRef
    private Directory directory;
    private ProgramLanguage language;
    private int currentVersion;
    private int versions;
    @DBRef
    private Project forkedFrom;

    public Project(String projectName, ProgramLanguage language, User owner) {
        this.projectName = projectName;
        this.language = language;
        this.currentVersion = 1;
        this.versions = 1;
        this.owner = owner;
        this.usersRoles = new HashMap<>();
        this.usersRoles.put(owner.getId() , Role.OWNER);
    }

    public Project() {
        this.usersRoles = new HashMap<>();
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

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public ProgramLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ProgramLanguage language) {
        this.language = language;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
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
}

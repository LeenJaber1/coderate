package com.coderate.backend.model;

import com.coderate.backend.enums.State;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "directories")
public class Directory {
    @Id
    private String id;
    @DBRef
    private List<Directory> subDirectories;
    @DBRef
    private List<File> files;
    @DBRef
    private Project project;
    @DBRef
    private User user;
    private Map<Integer, State> versionState;

    public Directory(User user, int version) {
        this.user = user;
        this.files = new ArrayList<>();
        this.subDirectories = new ArrayList<>();
        this.versionState = new HashMap<>();
        this.versionState.put(version, State.NEW);
    }

    public Directory() {
        this.files = new ArrayList<>();
        this.subDirectories = new ArrayList<>();
        this.versionState = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Directory> getSubDirectories() {
        return subDirectories;
    }

    public void setSubDirectories(List<Directory> subDirectories) {
        this.subDirectories = subDirectories;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<Integer, State> getVersionState() {
        return versionState;
    }

    public void setVersionState(Map<Integer, State> versionState) {
        this.versionState = versionState;
    }
}

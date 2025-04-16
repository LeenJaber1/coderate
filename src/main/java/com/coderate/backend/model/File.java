package com.coderate.backend.model;

import com.coderate.backend.enums.Role;
import com.coderate.backend.enums.State;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "files")
public class File {
    @Id
    private String id;
    // e.g. "Main.java"
    private String filePath;
    private List<String> lines;
    private int version;
    @DBRef
    private User user;
    @DBRef
    private Project project;
    @DBRef
    private Directory parentDirectory;
    private State state;
    @DBRef
    private List<User> editors;

    public File(List<String> lines, int version, User user , String filePath, Project project, Directory parentDirectory) {
        this.lines = lines;
        this.version = version;
        this.user = user;
        this.filePath = filePath;
        this.project = project;
        this.parentDirectory = parentDirectory;
        this.editors = new ArrayList<>();
        this.editors.add(user);
    }

    public File() {
        this.editors = new ArrayList<>();
        this.lines = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setParentDirectory(Directory parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public Directory getParentDirectory() {
        return parentDirectory;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<User> getEditors() {
        return editors;
    }

    public void setEditors(List<User> editors) {
        this.editors = editors;
    }
}

package com.coderate.backend.dto;

import com.coderate.backend.enums.State;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectStructure {
    private Map<String, String> parentDirectoryPath;
    private Map<String, List<String>> filesContents;
    private LinkedHashMap<String, State> directoriesState;
    private Map<String, State> filesState;
    private String projectName ;

    public ProjectStructure() {
        this.filesContents = new HashMap<>();
        this.parentDirectoryPath = new HashMap<>();
        this.filesState = new HashMap<>();
        this.directoriesState = new LinkedHashMap<>();
    }

    public ProjectStructure(Map<String, String> parentDirectoryPath, Map<String, List<String>> filesContents) {
        this.parentDirectoryPath = parentDirectoryPath;
        this.filesContents = filesContents;
    }

    public ProjectStructure(Map<String, String> parentDirectoryPath, Map<String, List<String>> filesContents, LinkedHashMap<String, State> directoriesState, Map<String, State> filesState) {
        this.parentDirectoryPath = parentDirectoryPath;
        this.filesContents = filesContents;
        this.directoriesState = directoriesState;
        this.filesState = filesState;
    }

    public Map<String, String> getParentDirectoryPath() {
        return parentDirectoryPath;
    }

    public void setParentDirectoryPath(Map<String, String> pathsParentDirectory) {
        this.parentDirectoryPath = pathsParentDirectory;
    }

    public Map<String, List<String>> getFilesContents() {
        return filesContents;
    }

    public void setFilesContents(Map<String, List<String>> filesContents) {
        this.filesContents = filesContents;
    }

    public Map<String, State> getFilesState() {
        return filesState;
    }

    public void setFilesState(Map<String, State> filesState) {
        this.filesState = filesState;
    }

    public LinkedHashMap<String, State> getDirectoriesState() {
        return directoriesState;
    }

    public void setDirectoriesState(LinkedHashMap<String, State> directoriesState) {
        this.directoriesState = directoriesState;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}

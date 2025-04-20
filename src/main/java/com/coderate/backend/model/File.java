package com.coderate.backend.model;

import com.coderate.backend.enums.State;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "files")
public class File extends AbstractStorageStructure{
    private List<String> lines;

    public File(int versionNumber, String versionId, String path, String userId, List<String> lines, String parentDirectoryId, State state, String projectId) {
        super(versionNumber, versionId, path, userId, parentDirectoryId, state, projectId);
        this.lines = lines;
    }

    public File(int versionNumber, String versionId, String path, String userId, String parentDirectoryId, State state, String projectId) {
        super(versionNumber, versionId, path, userId, parentDirectoryId, state, projectId);
    }


    public File() {
        this.lines = new ArrayList<>();
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }


}

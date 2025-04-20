package com.coderate.backend.model;

import com.coderate.backend.enums.State;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "directories")
public class Directory extends AbstractStorageStructure{
    private boolean isMain;
    public Directory(int versionNumber, String versionId, String path, String projectId, String userId, String parentDirectoryId, State state, boolean isMain) {
        super(versionNumber, versionId, path, userId, parentDirectoryId, state, projectId);
        this.isMain = isMain;
    }

    public Directory() {
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }
}

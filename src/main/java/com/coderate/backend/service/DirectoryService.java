package com.coderate.backend.service;

import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.User;

import java.util.List;

public interface DirectoryService {
    Directory createDirectory(User user, int version);
    void updateDirectory(List<File> files , String id) throws DirectoryNotFoundException;
    void addSubDirectory(Directory directory, String directoryId) throws DirectoryNotFoundException;
    void deleteDirectory(Directory directory);
    void removeFile(File file, String directoryId) throws DirectoryNotFoundException;
    void addFile(File file, String directoryId) throws DirectoryNotFoundException;
    void deleteSubDirectory(Directory subDirectory, String directoryId) throws DirectoryNotFoundException;
    void deleteSubDirectory(Directory subDirectory , Directory parentDirectory);
    void changeDirectoryState(String directoryId, State state, int version) throws DirectoryNotFoundException;
    List<Directory> getDirectoriesByProjectId(String projectId);
    void saveDirectory(Directory directory);
}

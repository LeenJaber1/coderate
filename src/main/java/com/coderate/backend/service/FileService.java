package com.coderate.backend.service;

import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.FileNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;

import java.util.List;

public interface FileService {
    File createFile(List<String> lines, int version, User user , String filePath, Project project, Directory parentDirectory);
    File updateFile(List<String> newLines, String fileId) throws FileNotFoundException;
    File loadFile(String fileId) throws FileNotFoundException;
    List<File> findFilesByProjectId(String projectId);
    List<File> findFilesByUserId(String userId);
    List<File> getFilesByVersion(int version);
    List<File> getFilesByDirectoryId(String directoryId);
    List<File> getFilesByDirectoryIdAndVersion(String directoryId, int version);
    void deleteFile(File file);
    List<File> getByProjectIdAndVersion(String projectId, int version);
    void changeFileState(String fileId , State state) throws FileNotFoundException;
    // add if owner or admin
    void addEditor(User user,  String fileId) throws FileNotFoundException;
    void saveFile(File file);
}

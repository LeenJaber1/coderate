package com.coderate.backend.service;

import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.MessageType;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.exceptions.FileNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.Version;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public interface StorageStructureService {
    //file path = parentDirectory path + file path
    File createFile(int versionNumber, String versionId, String path, String userId, String parentDirectoryId, String projectId, List<String> lines) throws DirectoryNotFoundException, FileAlreadyExistsException;

    Directory createDirectory(int versionNumber, String versionId, String path, String projectId, String userId, String parentDirectoryId, boolean isMain) throws DirectoryNotFoundException;

    //delete permanently
    void deleteFileFromRepo(String fileId) throws FileNotFoundException;

    void deleteDirectoryFromRepo(String directoryId) throws DirectoryNotFoundException;

    //create new versions where state = deleted
    void deleteFileFromProject(File file , Version currentVersion);

    void deleteDirectoryFromProject(Directory directory , Version currentVersion);

    //build structure based on if the files have one of the maximum versions in the list
    ProjectStructure getProjectStructure(String projectId , Integer version);

    ProjectStructure getVersionChanges(String projectId, Integer version);

    void deleteProjectStorage(String projectId);

    List<Directory> findLatestDirectoriesVersion(String projectId , Integer version);

    List<File> findLatestFilesVersion(String projectId , Integer version);

    void saveVersionUpdates(ProjectStructure newProjectStructure, Version latestVersion , String projectId , String userId, ProjectStructure latestProjectStructure,
                            Version parentVersion ) throws DirectoryNotFoundException, FileAlreadyExistsException;

    Directory getDirectoryById(String id) throws DirectoryNotFoundException;

    List<String> mergeFiles(List<String> latestVersionLines, List<String> newVersionLines, List<Integer> changedLines);

    List<Integer> compareTwoFiles(List<String> latestVersionLines, List<String> newVersionLines);

    String checkForMergeConflictsForFiles(List<String> oldFile , List<String> newFile , List<String> latestFile);

}

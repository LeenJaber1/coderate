package com.coderate.backend.service.impl;

import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.MessageType;
import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.exceptions.FileNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.Version;
import com.coderate.backend.repository.DirectoryRepository;
import com.coderate.backend.repository.FileRepository;
import com.coderate.backend.service.StorageStructureService;
import com.coderate.backend.service.VersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StorageStructureServiceImpl implements StorageStructureService {
    private FileRepository fileRepository;
    private DirectoryRepository directoryRepository;
    private VersionService versionService;

    public StorageStructureServiceImpl(FileRepository fileRepository, DirectoryRepository directoryRepository, VersionService versionService) {
        this.fileRepository = fileRepository;
        this.directoryRepository = directoryRepository;
        this.versionService = versionService;
    }

    @Transactional
    @Override
    public File createFile(int versionNumber, String versionId, String path, String userId, String parentDirectoryId, String projectId, List<String> lines) throws DirectoryNotFoundException, FileAlreadyExistsException {
        if(this.fileRepository.findByPathAndProjectIdAndVersionNumber(path , projectId, versionNumber).isPresent()){
            throw new FileAlreadyExistsException("file already exists");
        }
        Path path1 = Paths.get(path);
        File file = new File(versionNumber , versionId , path1.toString(), userId , parentDirectoryId , State.NEW , projectId);
        file.setLines(lines);
        this.fileRepository.save(file);
        return file;
    }


    @Transactional
    @Override
    public Directory createDirectory(int versionNumber, String versionId, String path, String projectId, String userId, String parentDirectoryId, boolean isMain) throws DirectoryNotFoundException {
        Path path1 = Paths.get(path);
        Directory newDirectory = new Directory(versionNumber , versionId , path1.toString() +  "\\", projectId, userId , parentDirectoryId , State.NEW, isMain);
        this.directoryRepository.save(newDirectory);
        return newDirectory;
    }

    @Transactional
    @Override
    public void deleteFileFromRepo(String fileId) throws FileNotFoundException {
        File file = this.fileRepository.findById(fileId).orElseThrow(()-> new FileNotFoundException("file doesn't exist"));
        this.fileRepository.delete(file);
    }

    @Transactional
    @Override
    public void deleteDirectoryFromRepo(String directoryId) throws DirectoryNotFoundException {
        Directory directory = this.directoryRepository.findById(directoryId).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        this.directoryRepository.delete(directory);
    }

    @Transactional
    @Override
    public void deleteFileFromProject(File file , Version currentVersion) {
        File deletedFile = new File(currentVersion.getVersionNumber() , currentVersion.getId(), file.getPath(), currentVersion.getUserId() , file.getLines() ,
                file.getParentDirectoryId(), State.DELETED , file.getProjectId());
        this.fileRepository.save(deletedFile);
    }

    @Transactional
    @Override
    public void deleteDirectoryFromProject(Directory directory , Version currentVersion) {
        Directory deletedDirectory = new Directory(currentVersion.getVersionNumber() , currentVersion.getId() , directory.getPath(), directory.getProjectId(),
                directory.getUserId(), directory.getParentDirectoryId() , State.DELETED ,directory.isMain());
        this.directoryRepository.delete(deletedDirectory);
    }


    @Override
    public List<Directory> findLatestDirectoriesVersion(String projectId, Integer version){
        return this.directoryRepository.findLatestVersionDirectoriesInProject(projectId , version);
    }

    @Override
    public List<File> findLatestFilesVersion(String projectId , Integer version){
        return this.fileRepository.findLatestVersionFilesInProject(projectId, version);
    }

    // take the new changes
    @Transactional
    @Override
    public void saveVersionUpdates(ProjectStructure newProjectStructure, Version latestVersion, String projectId, String userId, ProjectStructure latestProjectStructure , Version parentVersion) throws DirectoryNotFoundException, FileAlreadyExistsException {
        LinkedHashMap<String, State> directoriesState = newProjectStructure.getDirectoriesState();
        Map<String, State> filesState = newProjectStructure.getFilesState();
        Map<String , List<String>> filesContent = newProjectStructure.getFilesContents();
        merge(newProjectStructure , latestProjectStructure,  parentVersion);
        addDirectoryChanges(latestVersion, projectId , userId , directoriesState);
        addFileChanges(filesState, filesContent , latestVersion , projectId ,userId);
    }

    @Transactional
    private void merge(ProjectStructure newProjectStructure, ProjectStructure latestProjectStructure, Version parentVersion ) {
        Map<String, List<String>> fileLinesMap = findLatestFilesVersion(parentVersion.getProjectId() , parentVersion.getVersionNumber())
                .stream()
                .filter(file -> file.getState() != State.DELETED)
                .collect(Collectors.toMap(
                        File::getPath,
                        File::getLines
                ));

        for(Map.Entry<String, List<String>> entry : newProjectStructure.getFilesContents().entrySet()){
            List<Integer> changedLines = compareTwoFiles(fileLinesMap.get(entry.getKey()), entry.getValue());
            List<String> mergeTwoFiles = mergeFiles(latestProjectStructure.getFilesContents().get(entry.getKey()), entry.getValue() , changedLines);
            entry.setValue(mergeTwoFiles);
        }
    }

    @Override
    public Directory getDirectoryById(String id) throws DirectoryNotFoundException {
        return this.directoryRepository.findById(id).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
    }

    @Transactional
    @Override
    public List<String> mergeFiles(List<String> latestVersionLines, List<String> newVersionLines , List<Integer> changedLines) {
        List<String> merged;
        if(latestVersionLines == null){
            return newVersionLines;
        }
        else{
            merged = new ArrayList<>(latestVersionLines);
        }

        for (Integer lineIndex : changedLines) {
            // Ensure index is within bounds
            lineIndex -=1;
            if (lineIndex >= 0 && lineIndex < newVersionLines.size()) {
                if (lineIndex < merged.size()) {
                    // Replace existing line
                    merged.set(lineIndex, newVersionLines.get(lineIndex));
                } else {
                    // If the line does not exist yet (e.g. appended lines), add it
                    merged.add(newVersionLines.get(lineIndex));
                }
            }
        }

        return merged;
    }

    @Override
    public List<Integer> compareTwoFiles(List<String> latestVersionLines, List<String> newVersionLines) {
        List<Integer> changedLines = new ArrayList<>();
        if(latestVersionLines == null){
            return changedLines;
        }
        int maxLength = Math.max(latestVersionLines.size(), newVersionLines.size());

        for (int i = 0; i < maxLength; i++) {
            String latestLine = i < latestVersionLines.size() ? latestVersionLines.get(i) : "";
            String newLine = i < newVersionLines.size() ? newVersionLines.get(i) : "";

            if (!Objects.equals(latestLine, newLine)) {
                changedLines.add(i + 1);
            }
        }
        return changedLines;
    }

    @Override
    public String checkForMergeConflictsForFiles(List<String> oldFile, List<String> newFile, List<String> latestFile) {
        List<Integer> compareOldWithLatest = compareTwoFiles(oldFile , latestFile);
        List<Integer> compareOldWithNew = compareTwoFiles(oldFile , newFile);

        Set<Integer> conflicts = new HashSet<>(compareOldWithLatest);
        conflicts.retainAll(compareOldWithNew); // only lines modified in both versions

        StringBuilder result = new StringBuilder();

        for (Integer lineNumber : conflicts) {
            String oldLine = lineNumber - 1 < oldFile.size() ? oldFile.get(lineNumber - 1) : "";
            String latestLine = lineNumber - 1 < latestFile.size() ? latestFile.get(lineNumber -1) : "";
            String newLine = lineNumber - 1< newFile.size() ? newFile.get(lineNumber - 1) : "";

            // Only add conflict if both changed and to different content
            if (!Objects.equals(latestLine, newLine)) {
                result.append("Conflict at line ").append(lineNumber).append(":\n");
                result.append("OLD    : ").append(oldLine).append("\n");
                result.append("LATEST : ").append(latestLine).append("\n");
                result.append("NEW    : ").append(newLine).append("\n\n");
            }
        }

        return result.toString();

    }


    @Transactional
    private void addDirectoryChanges(Version latestVersion, String projectId, String userId,  LinkedHashMap<String, State> directoriesState) throws DirectoryNotFoundException {
        for(Map.Entry<String, State> entry : directoriesState.entrySet()){
            Directory directory = this.directoryRepository.findTopDirectoryInProjectByPath(projectId, latestVersion.getVersionNumber(), entry.getKey()).orElse(null);
            if (entry.getValue() == State.DELETED){
                deleteDirectoryFromProject(directory, latestVersion);
            }
            else{
                //new
                String parentId = "";
                if(directory!= null){
                    parentId = directory.getParentDirectoryId();
                }
                else{
                    Path subDirectoryPath = Paths.get(entry.getKey());
                    Path parent = subDirectoryPath.getParent();
                    if(parent == null){
                        continue;
                    }
                    Directory parentDirectory = this.directoryRepository.findTopDirectoryInProjectByPath(projectId, latestVersion.getVersionNumber(),parent.toString() + "\\").orElse(null);
                    parentId = parentDirectory.getId();
                }
                Directory newDirectory = createDirectory(latestVersion.getVersionNumber() , latestVersion.getId(), entry.getKey(), projectId, userId , parentId,
                        false);
            }
        }
    }

    @Transactional
    private void addFileChanges(Map<String, State> filesState , Map<String , List<String>> filesContent, Version latestVersion, String projectId, String userId ) throws FileAlreadyExistsException, DirectoryNotFoundException {
        for(Map.Entry<String, State> entry : filesState.entrySet()){
            File file = this.fileRepository.findTopFileInProjectByPath(projectId, latestVersion.getVersionNumber(), entry.getKey()).orElse(null);
            if (entry.getValue() == State.DELETED){
                deleteFileFromProject(file, latestVersion);
            }
            else{
                String parentId = "";
                if(entry.getValue() == State.NEW){
                    Path subDirectoryPath = Paths.get(entry.getKey());
                    Path parent = subDirectoryPath.getParent();
                    Directory parentDirectory = this.directoryRepository.findTopDirectoryInProjectByPath(projectId, latestVersion.getVersionNumber(),parent.toString() + "\\").orElse(null);
                    parentId = parentDirectory.getId();
                }
                else{
                    parentId = file.getParentDirectoryId();
                }
                State state = entry.getValue();
                // int versionNumber, String versionId, String path, String userId, String parentDirectoryId, String projectId, List<String> lines
                File newFile = createFile(latestVersion.getVersionNumber(), latestVersion.getId(), entry.getKey(), userId, parentId, projectId , filesContent.get(entry.getKey()));
                newFile.setState(state);
                this.fileRepository.save(newFile);
            }
        }
    }

    @Override
    public ProjectStructure getProjectStructure(String projectId, Integer version) {
        List<Directory> directories = this.directoryRepository.findLatestVersionDirectoriesInProject(projectId , version);
        List<File> files = this.fileRepository.findLatestVersionFilesInProject(projectId, version);

        Map<String, String> directoriesId = directories.stream()
                .filter(directory -> directory.getState() != State.DELETED)
                .collect(Collectors.toMap(
                        Directory::getId,
                        Directory::getPath
                ));
        directoriesId.put("","");


        Map<String , String> parentDirectoryPath = new HashMap<>();

        Map<String, List<String>> filesContents = files.stream()
                .filter(file -> file.getState() != State.DELETED)
                .collect(Collectors.toMap(File::getPath, File::getLines));

        LinkedHashMap<String, State> directoriesState = directories.stream()
                .collect(Collectors.toMap(
                        Directory::getPath,
                        Directory::getState,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));


        Map<String, State> filesState = files.stream()
                .collect(Collectors.toMap(
                        File::getPath,
                        File::getState
                ));

        buildProjectStructure(directoriesId , files , parentDirectoryPath, directories, filesContents);

        return new ProjectStructure(parentDirectoryPath, filesContents, directoriesState , filesState);
    }

    @Override
    public ProjectStructure getVersionChanges(String projectId, Integer version) {
        List<Directory> directories = this.directoryRepository.findByProjectIdAndVersionNumber(projectId , version);
        List<File> files = this.fileRepository.findByProjectIdAndVersionNumber(projectId, version);

        Map<String, List<String>> filesContents = files.stream()
                .filter(file -> file.getState() != State.DELETED)
                .collect(Collectors.toMap(File::getPath, File::getLines));

        LinkedHashMap<String, State> directoriesState = directories.stream()
                .collect(Collectors.toMap(
                        Directory::getPath,
                        Directory::getState,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));

        Map<String, State> filesState = files.stream()
                .collect(Collectors.toMap(
                        File::getPath,
                        File::getState
                ));

        Map<String , String> parentDirectoryPath = new HashMap<>();

        return new ProjectStructure(parentDirectoryPath, filesContents, directoriesState , filesState);
    }

    @Transactional
    @Override
    public void deleteProjectStorage(String projectId) {
        this.fileRepository.deleteAllByProjectId(projectId);
        this.directoryRepository.deleteAllByProjectId(projectId);
    }


    private void buildProjectStructure(Map<String, String> directoriesId, List<File> files, Map<String, String> parentDirectoryPath, List<Directory> directories,
                                       Map<String, List<String>> filesContents) {
        // if parent Directory is not there then the file is deleted
        for(File file : files){
            if(directoriesId.containsKey(file.getParentDirectoryId())){
                parentDirectoryPath.put(file.getPath() , directoriesId.get(file.getParentDirectoryId()));
            }
            else{
                filesContents.remove(file.getId());
            }
        }
        for(Directory directory : directories){
            if(directoriesId.containsKey(directory.getParentDirectoryId())){
                parentDirectoryPath.put(directory.getPath() , directoriesId.get(directory.getParentDirectoryId()));
            }
        }
    }
}

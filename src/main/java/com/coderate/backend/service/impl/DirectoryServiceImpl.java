package com.coderate.backend.service.impl;

import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.DirectoryNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.User;
import com.coderate.backend.repository.DirectoryRepository;
import com.coderate.backend.service.DirectoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DirectoryServiceImpl implements DirectoryService {
    private DirectoryRepository directoryRepository;

    public DirectoryServiceImpl(DirectoryRepository directoryRepository){
        this.directoryRepository = directoryRepository;
    }

    @Override
    public Directory createDirectory(User user, int version) {
        Directory directory = new Directory(user, version);
        return this.directoryRepository.save(directory);
    }

    @Override
    public void updateDirectory(List<File> files, String id) throws DirectoryNotFoundException {
        Directory directory = this.directoryRepository.findById(id).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        directory.setFiles(files);
        this.directoryRepository.save(directory);
    }

    // when opening a file keep track of its directory id
    @Override
    public void addSubDirectory(Directory directory, String directoryId) throws DirectoryNotFoundException {
        Directory parentDirectory = this.directoryRepository.findById(directoryId).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        parentDirectory.getSubDirectories().add(directory);
        this.directoryRepository.save(parentDirectory);
    }

    @Override
    public void deleteDirectory(Directory directory) {
        this.directoryRepository.delete(directory);
    }

    @Override
    public void removeFile(File file, String directoryId) throws DirectoryNotFoundException {
        Directory directory = this.directoryRepository.findById(directoryId).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        directory.getFiles().remove(file);
        this.directoryRepository.save(directory);
    }

    @Override
    public void addFile(File file, String directoryId) throws DirectoryNotFoundException {
        Directory directory = this.directoryRepository.findById(directoryId).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        directory.getFiles().add(file);
        this.directoryRepository.save(directory);
    }

    @Override
    public void deleteSubDirectory(Directory subDirectory, String directoryId) throws DirectoryNotFoundException {
        Directory directory = this.directoryRepository.findById(directoryId).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        directory.getSubDirectories().remove(subDirectory);
        this.directoryRepository.save(directory);

    }

    @Override
    public void deleteSubDirectory(Directory subDirectory, Directory parentDirectory) {
        parentDirectory.getSubDirectories().remove(subDirectory);
        this.directoryRepository.save(parentDirectory);
    }

    @Override
    public void changeDirectoryState(String directoryId, State state, int version) throws DirectoryNotFoundException {
        Directory directory = this.directoryRepository.findById(directoryId).orElseThrow(() -> new DirectoryNotFoundException("directory not found"));
        directory.getVersionState().put(version, state);
        this.directoryRepository.save(directory);
    }

    @Override
    public List<Directory> getDirectoriesByProjectId(String projectId) {
        return this.directoryRepository.findByProjectId(projectId);
    }

    @Override
    public void saveDirectory(Directory directory) {
        this.directoryRepository.save(directory);
    }
}

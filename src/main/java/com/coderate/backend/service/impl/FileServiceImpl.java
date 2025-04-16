package com.coderate.backend.service.impl;

import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.FileNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import com.coderate.backend.repository.FileRepository;
import com.coderate.backend.service.FileService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FileServiceImpl implements FileService {
    private FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public void setFileRepository(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public File createFile(List<String> lines, int version, User user, String filePath, Project project, Directory parentDirectory) {
        File file = new File(lines, version, user , filePath, project, parentDirectory);
        this.fileRepository.save(file);
        return file;
    }

    @Override
    public File updateFile(List<String> newLines, String fileId) throws FileNotFoundException {
        File file = this.loadFile(fileId);
        file.setLines(newLines);
        return file;
    }

    @Override
    public File loadFile(String fileId) throws FileNotFoundException {
        return this.fileRepository.findById(fileId).orElseThrow(() -> new FileNotFoundException());
    }

    @Override
    public List<File> findFilesByProjectId(String projectId) {
        return this.fileRepository.findByProjectId(projectId);
    }

    @Override
    public List<File> findFilesByUserId(String userId) {
        return this.fileRepository.findByUserId(userId);
    }

    @Override
    public List<File> getFilesByVersion(int version) {
        return this.fileRepository.findByVersion(version);
    }

    @Override
    public List<File> getFilesByDirectoryId(String directoryId) {
        return this.fileRepository.findByDirectoryId(directoryId);
    }

    @Override
    public List<File> getFilesByDirectoryIdAndVersion(String directoryId, int version) {
        return this.fileRepository.findByDirectoryAndVersion(directoryId , version);
    }

    @Override
    public void deleteFile(File file) {
        this.fileRepository.delete(file);
    }

    @Override
    public List<File> getByProjectIdAndVersion(String projectId, int version) {
        return this.fileRepository.findByProjectAndVersion(projectId, version);
    }

    @Override
    public void changeFileState(String fileId, State state) throws FileNotFoundException {
        File file = this.loadFile(fileId);
        file.setState(state);
        this.fileRepository.save(file);
    }

    @Override
    public void addEditor(User user, String fileId) throws FileNotFoundException {
        File file = this.loadFile(fileId);
        file.getEditors().add(user);
        this.fileRepository.save(file);
    }

    @Override
    public void saveFile(File file) {
        this.fileRepository.save(file);
    }
}

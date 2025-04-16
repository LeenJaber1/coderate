package com.coderate.backend.service.impl;

import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.NotAuthorizedException;
import com.coderate.backend.exceptions.ProjectNotFoundException;
import com.coderate.backend.model.Directory;
import com.coderate.backend.model.File;
import com.coderate.backend.model.Project;
import com.coderate.backend.model.User;
import com.coderate.backend.repository.ProjectRepository;
import com.coderate.backend.service.DirectoryService;
import com.coderate.backend.service.FileService;
import com.coderate.backend.service.ProjectService;
import com.coderate.backend.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private FileService fileService;
    private DirectoryService directoryService;
    private UserService userService;

    public ProjectServiceImpl(ProjectRepository projectRepository, FileService fileService, DirectoryService directoryService, UserService userService) {
        this.projectRepository = projectRepository;
        this.directoryService = directoryService;
        this.fileService = fileService;
        this.userService = userService;
    }


    @Override
    public Project createProject(String projectName, ProgramLanguage language, User owner) {
        Project project = new Project(projectName , language, owner);
        Directory directory = this.directoryService.createDirectory(owner, project.getCurrentVersion());
        project.setDirectory(directory);
        this.projectRepository.save(project);
        directory.setProject(project);
        this.directoryService.saveDirectory(directory);
        return project;
    }

    @Override
    public void changeProjectName(String newName, String projectId, User owner) throws ProjectNotFoundException, NotAuthorizedException {
        Project project = getProjectById(projectId);
        if(!project.getOwner().getId().equals(owner.getId())){
            throw new NotAuthorizedException("Not Authorized to change project name");
        }
        project.setProjectName(newName);
        this.projectRepository.save(project);
    }

    @Override
    public Project getProjectById(String projectId) throws ProjectNotFoundException {
        return this.projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException("project not found"));
    }

    @Override
    public Project changeProjectVersion(int version, String projectId) throws ProjectNotFoundException {
        Project project = getProjectById(projectId);
        List<Directory> directories = this.directoryService.getDirectoriesByProjectId(projectId);
        List<Directory> deletedDirectories = new ArrayList<>();
        for(Directory directory : directories){
            if(directory.getVersionState().get(version)!= State.DELETED){
                List<File> files = this.fileService.getFilesByDirectoryIdAndVersion(directory.getId(), version)
                        .stream()
                        .filter(file -> file.getState() != State.DELETED)
                        .collect(Collectors.toList());;
                directory.setFiles(files);
                this.directoryService.saveDirectory(directory);
            }
            else{
                deletedDirectories.add(directory);
            }
        }
        removeDeletedDirectories(deletedDirectories, project.getDirectory());

        return null;
    }

    private void removeDeletedDirectories(List<Directory> deletedDirectories, Directory mainDirectory) {
        for (Directory directory : mainDirectory.getSubDirectories()){
            if(deletedDirectories.contains(directory)){
                removeDeletedDirectories(deletedDirectories, directory);
                this.directoryService.deleteSubDirectory(directory, mainDirectory);
            }
        }
    }

    @Override
    public void addUserRole(User user, Role role, User ownerAdmin, Project project) throws NotAuthorizedException {
        if(!project.getOwner().getId().equals(ownerAdmin.getId()) && project.getUsersRoles().get(ownerAdmin)!= Role.ADMIN){
            throw new NotAuthorizedException("not authorized to add users");
        }
        project.getUsersRoles().put(user.getUsername() , role);
        this.projectRepository.save(project);
    }

    @Override
    public void addNewFileToDirectory(String projectId, File file, Directory directory) {

    }

    @Override
    public void addNewDirectory(String projectId, Directory directory, Directory parentDirectory) {

    }

    @Override
    public List<UserDetails> getUsersByRole(Project project, Role role) {
        return project.getUsersRoles().entrySet().stream()
                .filter(entry -> entry.getValue() == role)
                .map(Map.Entry::getKey)
                .map(userId -> {
                    return userService.loadUserByUsername(userId);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public User getProjectOwner(String projectId) throws ProjectNotFoundException {
        Project project = getProjectById(projectId);
        return project.getOwner();
    }

    @Override
    public void deleteProject(String projectId, User user) throws ProjectNotFoundException, NotAuthorizedException {
        Project project = getProjectById(projectId);
        if(!project.getOwner().getId().equals(user.getId())){
            throw new NotAuthorizedException("Not authorized to delete project");
        }
        deleteFiles(project.getDirectory());
        this.directoryService.deleteDirectory(project.getDirectory());
        this.projectRepository.delete(project);
    }

    private void deleteFiles(Directory directory){
        for(File file : directory.getFiles()){
            this.fileService.deleteFile(file);
        }
        for(Directory directory1 : directory.getSubDirectories()){
            deleteFiles(directory1);
            this.directoryService.deleteDirectory(directory1);
        }
    }

    @Override
    public void removeUserRole(User user, User ownerAdmin, Project project) throws NotAuthorizedException {
        if(!project.getOwner().getId().equals(ownerAdmin.getId()) && project.getUsersRoles().get(ownerAdmin)!= Role.ADMIN){
            throw new NotAuthorizedException("not authorized to add users");
        }
        project.getUsersRoles().remove(user);
        this.projectRepository.save(project);
    }

    @Override
    public Project forkProject(String projectId, User user) throws ProjectNotFoundException {
        Project project = getProjectById(projectId);
        Project forkedProject = new Project();
        forkedProject.setForkedFrom(project);
        copyProjectMetaData(project, forkedProject, user);
        copyProjectFiles(project, forkedProject);
        this.projectRepository.save(forkedProject);
        return forkedProject;
    }

    @Override
    public Project cloneProject(String projectId, User user) throws ProjectNotFoundException {
        Project project = getProjectById(projectId);
        Project clone = new Project();
        copyProjectMetaData(project, clone, user);
        copyProjectFiles(project, clone);
        this.projectRepository.save(clone);
        return clone;
    }

    private void copyProjectFiles(Project originalProject , Project clonedProject){
        Directory newDirectory = new Directory(clonedProject.getOwner() ,clonedProject.getCurrentVersion());
        clonedProject.setDirectory(newDirectory);
        newDirectory.setProject(clonedProject);
        copyDirectoriesAndFiles(originalProject.getDirectory(), newDirectory, clonedProject);
    }

    private void copyDirectoriesAndFiles(Directory originalDirectory, Directory clonedDirectory, Project clonedProject){
        for(File file : originalDirectory.getFiles()){
            File newFile = new File(file.getLines(), 1 , clonedProject.getOwner(), file.getFilePath(), clonedProject, clonedDirectory);
            clonedDirectory.getFiles().add(newFile);
            this.fileService.saveFile(file);
        }
        for(Directory directory : originalDirectory.getSubDirectories()){
            Directory newDirectory = new Directory(clonedProject.getOwner(), clonedProject.getCurrentVersion());
            clonedDirectory.getSubDirectories().add(newDirectory);
            newDirectory.setProject(clonedProject);
            copyDirectoriesAndFiles(directory, newDirectory, clonedProject);
            this.directoryService.saveDirectory(newDirectory);
        }
        this.directoryService.saveDirectory(clonedDirectory);
    }

    private void copyProjectMetaData(Project original , Project clone, User user){
        clone.setProjectName(original.getProjectName() + " (Copy)");
        clone.setOwner(user);
        clone.setCurrentVersion(1);
        clone.setVersions(1);
        clone.getUsersRoles().put(user.getUsername(), Role.OWNER);
        user.getProjects().add(clone);
    }


}

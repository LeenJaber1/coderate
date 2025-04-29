package com.coderate.backend.service.impl;

import com.coderate.backend.dto.ProjectStructure;
import com.coderate.backend.enums.ProgramLanguage;
import com.coderate.backend.enums.Role;
import com.coderate.backend.enums.State;
import com.coderate.backend.exceptions.*;
import com.coderate.backend.model.*;
import com.coderate.backend.repository.ProjectRepository;
import com.coderate.backend.response.ConflictResponse;
import com.coderate.backend.service.ProjectService;
import com.coderate.backend.service.StorageStructureService;
import com.coderate.backend.service.UserService;
import com.coderate.backend.service.VersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private UserService userService;
    private StorageStructureService storageStructureService;
    private VersionService versionService;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserService userService,
                              StorageStructureService storageStructureService, VersionService versionService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.versionService = versionService;
        this.storageStructureService = storageStructureService;
    }


    @Override
    public boolean canEnterSessions(String projectId, String userId) throws ProjectNotFoundException {
        Project project = getProjectById(projectId);
        return project.getUsersRoles().containsKey(userId);
    }

    @Transactional
    @Override
    public Project createProject(String projectName, ProgramLanguage language, User owner) throws DirectoryNotFoundException {
        Project project = new Project(projectName, language, owner);
        this.projectRepository.save(project);
        Version version = this.versionService.createNewVersion(null , owner.getId(), project.getId(), 1);
        version.setRoot(true);
        this.versionService.saveVersion(version);
        Directory mainDirectory = this.storageStructureService.createDirectory(1 , version.getId() , projectName  , project.getId(),
                owner.getId(), "" , true);
        project.setCurrentVersion(version);
        project.getVersionUserOn().put(owner.getId(), version.getVersionNumber());
        project.setMainDirectoryId(mainDirectory.getId());
        updateNewVersion(project);
        this.projectRepository.save(project);
        return project;
    }

    @Transactional
    private void updateNewVersion(Project project){
        project.setLatestVersion(project.getLatestVersion() + 1);
    }

    @Transactional
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

    @Transactional
    @Override
    public void addUserRole(String userId, Role role, User ownerAdmin, Project project) throws NotAuthorizedException {
        if(!project.getOwner().getId().equals(ownerAdmin.getId()) && project.getUsersRoles().get(ownerAdmin.getId())!= Role.ADMIN){
            throw new NotAuthorizedException("not authorized to add users");
        }
        project.getUsersRoles().put(userId , role);
        project.getVersionUserOn().put(userId, project.getCurrentVersion().getVersionNumber());
        this.projectRepository.save(project);
    }

    @Override
    public User getProjectOwner(String projectId) throws ProjectNotFoundException {
        Project project = getProjectById(projectId);
        return project.getOwner();
    }

    @Transactional
    @Override
    public void deleteProject(String projectId, User user) throws ProjectNotFoundException, NotAuthorizedException {
        Project project = getProjectById(projectId);
        if(!project.getOwner().getId().equals(user.getId())){
            throw new NotAuthorizedException("Not authorized to delete project");
        }
        this.storageStructureService.deleteProjectStorage(projectId);
        this.projectRepository.delete(project);
        this.versionService.deleteVersion(projectId);
    }


    @Transactional
    @Override
    public void removeUserRole(String user, User ownerAdmin, Project project) throws NotAuthorizedException {
        if(!project.getOwner().getId().equals(ownerAdmin.getId()) && project.getUsersRoles().get(ownerAdmin.getId())!= Role.ADMIN){
            throw new NotAuthorizedException("not authorized to remove users");
        }
        project.getUsersRoles().remove(user);
        this.projectRepository.save(project);
    }


    @Override
    public ProjectStructure changeVersion(int versionNumber, String projectId, String userId) throws ProjectNotFoundException, VersionNotFoundException {
        Project project = getProjectById(projectId);
        project.getVersionUserOn().put(userId , versionNumber);
        this.projectRepository.save(project);
        ProjectStructure projectStructure = this.storageStructureService.getProjectStructure(projectId , versionNumber);
        projectStructure.setProjectName(project.getProjectName());
        return projectStructure;
    }

    @Override
    public ProjectStructure getVersion(int versionNumber, String projectId) {
        return this.storageStructureService.getVersionChanges(projectId , versionNumber);
    }

    @Transactional
    @Override
    public ProjectStructure changeProjectVersion(int version, String projectId, String userId) throws VersionNotFoundException, ProjectNotFoundException, NotAuthorizedException {
        Project project = getProjectById(projectId);
        if(!project.getOwner().getId().equals(userId) && project.getUsersRoles().get(userId)!= Role.ADMIN){
            throw new NotAuthorizedException("not authorized to change project version");
        }
        Version targetVersion = this.versionService.getVersionByVersionNumber(version , projectId);
        project.setCurrentVersion(targetVersion);
        this.projectRepository.save(project);
        return this.storageStructureService.getProjectStructure(projectId , project.getLatestVersion());
    }

    @Transactional
    @Override
    public ConflictResponse saveVersion(String userId, String projectId, String message , ProjectStructure newProjectStructure) throws ProjectNotFoundException, VersionNotFoundException, NotAuthorizedException, DirectoryNotFoundException, FileAlreadyExistsException {
        Project project = getProjectById(projectId);
        // get only the files that changed
        ProjectStructure latestVersionStructure = getVersion(project.getLatestVersion() , projectId);
        Version parentVersion = this.versionService.getVersionByVersionNumber(project.getVersionUserOn().get(userId) , projectId);

        ConflictResponse conflictResponse = checkForMergeConflictsWithLatestVersion(parentVersion , latestVersionStructure, newProjectStructure);
        if(!conflictResponse.getFileConflictLines().isEmpty() || !conflictResponse.getStatesThatDiffer().isEmpty()){
            return conflictResponse;
        }

        updateNewVersion(project);
        Version newVersion = this.versionService.createNewVersion(project.getCurrentVersion(), userId,  projectId, project.getLatestVersion());
        // get the merged files stuff
        this.storageStructureService.saveVersionUpdates(newProjectStructure, newVersion, projectId , userId, latestVersionStructure ,parentVersion);

        newVersion.setMessage(message);
        project.setCurrentVersion(newVersion);
        project.getVersionUserOn().put(userId, newVersion.getVersionNumber());
        this.versionService.saveVersion(newVersion);
        this.projectRepository.save(project);

        return conflictResponse;
    }

    // if changed the same line etc.
    // case 1:
    // get the files that changed in the newChange request and compare them with parent version to see what lines changed in those files
    // take those same files in the latest version structure and compare them with the files in the parent version and see what lines changed
    // if same lines changed then conflict occurred, if no merge two files
    // case 2:
    // if a file is deleted in one of the versions but the other no (or a directory)
    private ConflictResponse checkForMergeConflictsWithLatestVersion(Version parentVersion , ProjectStructure latestStructure , ProjectStructure newChanges) throws VersionNotFoundException {
        Map<String, List<String>> fileLinesMap = this.storageStructureService.findLatestFilesVersion(parentVersion.getProjectId() , parentVersion.getVersionNumber())
                .stream()
                .filter(file -> file.getState() != State.DELETED)
                .collect(Collectors.toMap(
                        File::getPath,
                        File::getLines
                ));


        ConflictResponse conflictResponse = new ConflictResponse();
        Map<String , String> fileConflictLines = conflictResponse.getFileConflictLines();
        Map<String , List<State>>  statesThatDiffer = conflictResponse.getStatesThatDiffer();

        checkForStates(latestStructure , newChanges , statesThatDiffer);

        checkForLineConflicts(latestStructure , newChanges , fileLinesMap , fileConflictLines);
        return  conflictResponse;
    }

    private void checkForLineConflicts(ProjectStructure latestStructure, ProjectStructure newChanges, Map<String, List<String>> fileLinesMap,
                                       Map<String , String> fileConflictLines) {
        for(Map.Entry<String , List<String>> entry : fileLinesMap.entrySet()){
            String filePath = entry.getKey();
            if(latestStructure.getFilesContents().containsKey(filePath) && newChanges.getFilesContents().containsKey(filePath)){
                String conflict = this.storageStructureService.checkForMergeConflictsForFiles(entry.getValue() , newChanges.getFilesContents().get(filePath),
                        latestStructure.getFilesContents().get(entry.getKey()));
                if(!conflict.isEmpty()){
                    fileConflictLines.put(filePath , conflict);
                }
            }
        }
    }

    private void checkForStates(ProjectStructure latestStructure, ProjectStructure newChanges, Map<String , List<State>> statesThatDiffer) {
        checkDirectoriesState(latestStructure , newChanges , statesThatDiffer);
        checkFilesState(latestStructure , newChanges, statesThatDiffer);
    }

    private void checkFilesState(ProjectStructure latestStructure, ProjectStructure newChanges, Map<String , List<State>>  statesThatDiffer) {
        for(Map.Entry<String, State> entry : latestStructure.getFilesState().entrySet()){
            if((entry.getValue() == State.NEW || entry.getValue() == State.MODIFIED) && newChanges.getFilesState().get(entry.getKey()) == State.DELETED){
                statesThatDiffer.put(entry.getKey(), List.of(entry.getValue(), State.DELETED));
            }
        }

        for(Map.Entry<String, State> entry : newChanges.getFilesState().entrySet()){
            if((entry.getValue() == State.NEW || entry.getValue() == State.MODIFIED) && latestStructure.getFilesState().get(entry.getKey()) == State.DELETED){
                statesThatDiffer.put(entry.getKey(), List.of(entry.getValue(), State.DELETED));
            }
        }
    }

    private void checkDirectoriesState(ProjectStructure latestStructure, ProjectStructure newChanges, Map<String , List<State>>  statesThatDiffer) {
        for(Map.Entry<String, State> entry : latestStructure.getDirectoriesState().entrySet()){
            if(entry.getValue() == State.NEW  && newChanges.getDirectoriesState().get(entry.getKey()) == State.DELETED){
                statesThatDiffer.put(entry.getKey(), List.of(entry.getValue(), State.DELETED));
            }
        }

        for(Map.Entry<String, State> entry : newChanges.getDirectoriesState().entrySet()){
            if(entry.getValue() == State.NEW  && latestStructure.getDirectoriesState().get(entry.getKey()) == State.DELETED){
                statesThatDiffer.put(entry.getKey(), List.of(entry.getValue(), State.DELETED));
            }
        }
    }

    @Override
    public Project forkProject(String projectId, User user) throws ProjectNotFoundException, FileAlreadyExistsException, DirectoryNotFoundException {
        Project project = getProjectById(projectId);
        Project forkedProject = new Project();
        forkedProject.setForkedFrom(project);
        copyProjectMetaData(project, forkedProject, user);
        copyProjectFiles(project, forkedProject);
        this.projectRepository.save(forkedProject);
        return forkedProject;
    }

    @Override
    public Project cloneProject(String projectId, User user) throws ProjectNotFoundException, FileAlreadyExistsException, DirectoryNotFoundException {
        Project project = getProjectById(projectId);
        Project clone = new Project();
        copyProjectMetaData(project, clone, user);
        copyProjectFiles(project, clone);
        this.projectRepository.save(clone);
        return clone;
    }

    @Override
    public List<Project> getProjectsByUserId(String userId) {
        return this.projectRepository.findByUserIdInUsersRoles(userId);
    }

    private void copyProjectMetaData(Project original , Project clone, User user){
        clone.setProjectName(original.getProjectName() + " (Copy)");
        clone.setOwner(user);
        clone.getUsersRoles().put(user.getUsername(), Role.OWNER);
        this.userService.addProject(clone , user.getId());
    }

    private void copyProjectFiles(Project originalProject , Project clonedProject) throws DirectoryNotFoundException, FileAlreadyExistsException {
        int latestVersion = originalProject.getLatestVersion();
        String projectId = originalProject.getId();

        List<File> latestFiles = this.storageStructureService.findLatestFilesVersion(projectId , latestVersion).stream()
                .filter(file -> file.getState() != State.DELETED)
                .toList();

        List<Directory> latestDirectories = this.storageStructureService.findLatestDirectoriesVersion(projectId , latestVersion).stream()
                .filter(directory -> directory.getState() != State.DELETED)
                .sorted(Comparator.comparingInt(d -> d.getPath().length()))
                .collect(Collectors.toList());

        Map<String, String> directoriesId = new HashMap<>();
        Directory mainDirectory = this.storageStructureService.getDirectoryById(clonedProject.getMainDirectoryId());

        // copy all directories from the old project to the new project by creating new ones
        createCopyDirectories(latestDirectories , clonedProject, clonedProject.getMainDirectoryId() , directoriesId , mainDirectory.getPath());

        createCopyFiles(latestFiles , clonedProject , directoriesId);
    }

    private void createCopyFiles(List<File> latestFiles, Project clonedProject, Map<String, String> directoriesId) throws FileAlreadyExistsException, DirectoryNotFoundException {
        for(File file : latestFiles){
            Path filePath = Paths.get(file.getPath());
            Path parentDirectory = filePath.getParent();
            if(directoriesId.containsKey(parentDirectory.toString())){
                File newFile = this.storageStructureService.createFile(clonedProject.getLatestVersion(),  clonedProject.getCurrentVersion().getId(),
                        file.getPath(), clonedProject.getOwner().getId(), directoriesId.get(parentDirectory.toString()),
                        clonedProject.getId(), file.getLines());
            }
        }
    }

    private void createCopyDirectories(List<Directory> latestDirectories, Project project, String mainDirectoryId, Map<String, String> directoriesId , String parentPath) throws DirectoryNotFoundException {
        for(Directory directory : latestDirectories){
            Path subDirectoryPath = Paths.get(directory.getPath());
            Path parent = subDirectoryPath.getParent();
            if(parent.toString().equals(parentPath)){
                Directory newDirectory = this.storageStructureService.createDirectory(project.getLatestVersion(), project.getCurrentVersion().getId(),
                        directory.getPath(), project.getId(), project.getOwner().getId() , mainDirectoryId, false);


                directoriesId.put(newDirectory.getPath() , newDirectory.getId());
                createCopyDirectories(latestDirectories, project , newDirectory.getId(), directoriesId , newDirectory.getPath());
            }
        }
    }


}

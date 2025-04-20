package com.coderate.backend.service.impl;

import com.coderate.backend.exceptions.VersionNotFoundException;
import com.coderate.backend.model.Version;
import com.coderate.backend.repository.VersionRepository;
import com.coderate.backend.service.VersionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VersionServiceImpl implements VersionService {
    private VersionRepository versionRepository;

    public VersionServiceImpl(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    @Override
    public Version createNewVersion(Version prevVersion, String userId, String projectId, int versionNumber) {
        Version version = new Version(prevVersion , userId , projectId , versionNumber);
        this.versionRepository.save(version);
        return version;
    }

    @Override
    public List<Version> getVersionsByUserInProject(String userId, String projectId) {
        return this.versionRepository.findByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public String getVersionMessage(String versionId) throws VersionNotFoundException {
        return getVersionById(versionId).getMessage();
    }

    @Override
    public Version getVersionById(String versionId) throws VersionNotFoundException {
        return this.versionRepository.findById(versionId).orElseThrow(() -> new VersionNotFoundException("Version doesn't exist"));
    }

    @Override
    public Version getVersionByVersionNumber(int versionNumber, String projectId) throws VersionNotFoundException {
        return this.versionRepository.findByProjectIdAndVersionNumber(projectId, versionNumber).orElseThrow(() -> new VersionNotFoundException("version doesn't exist"));
    }

    @Override
    public List<Integer> getVersionsFromRootToDestination(int destinationVersion, String projectId) throws VersionNotFoundException {
        Version version = getVersionByVersionNumber(destinationVersion , projectId);
        List<Integer> versionsList = new ArrayList<>();
        while(version != null){
            versionsList.add(version.getVersionNumber());
            version = version.getPrevVersion();
        }
        return versionsList;
    }

    @Override
    public void saveVersion(Version version) {
        this.versionRepository.save(version);
    }

    @Override
    public void deleteVersion(String projectId) {
        this.versionRepository.deleteAllByProjectId(projectId);
    }
}

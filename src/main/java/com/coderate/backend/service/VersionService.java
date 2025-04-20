package com.coderate.backend.service;

import com.coderate.backend.exceptions.VersionNotFoundException;
import com.coderate.backend.model.Version;
import com.coderate.backend.model.File;

import java.util.List;

public interface VersionService {
    Version createNewVersion(Version prevVersion, String userId, String projectId, int versionNumber);

    List<Version> getVersionsByUserInProject(String userId, String projectId);

    String getVersionMessage(String versionId) throws VersionNotFoundException;

    Version getVersionById(String versionId) throws VersionNotFoundException;

    Version getVersionByVersionNumber(int versionNumber , String projectId) throws VersionNotFoundException;

    List<Integer> getVersionsFromRootToDestination(int destinationVersion, String projectId) throws VersionNotFoundException;

    void saveVersion(Version version);

    void deleteVersion(String projectId);
}

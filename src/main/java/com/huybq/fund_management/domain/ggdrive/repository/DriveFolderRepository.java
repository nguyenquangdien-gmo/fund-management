package com.huybq.fund_management.domain.ggdrive.repository;

import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveFolderRepository extends JpaRepository<DriveFolder, Long> {

    List<DriveFolder> findByParentFolderId(Long parentFolderId);

    Optional<DriveFolder> findByGoogleFolderId(String googleFolderId);

    List<DriveFolder> findByCreatedBy_Id(Long userId);

    boolean existsByNameAndParentFolder(String name, DriveFolder parentFolder);

    boolean existsByNameAndParentFolderIsNull(String name);

    Optional<DriveFolder> findByNameAndParentFolder(String name, DriveFolder parentFolder);

    Optional<DriveFolder> findByNameAndParentFolderIsNull(String name);
}

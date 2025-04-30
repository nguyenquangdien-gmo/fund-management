package com.huybq.fund_management.domain.ggdrive.repository;

import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.entity.GoogleDriveAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveFolderRepository extends JpaRepository<DriveFolder, Long> {
    List<DriveFolder> findByParentFolderId(Long parentFolderId);
    List<DriveFolder> findByGoogleDriveAccount(GoogleDriveAccount account);
    Optional<DriveFolder> findByGoogleFolderId(String googleFolderId);
}

package com.huybq.fund_management.domain.ggdrive.repository;

import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveFileRepository extends JpaRepository<DriveFile, Long> {

    List<DriveFile> findByFolder(DriveFolder folder);

    Optional<DriveFile> findByGoogleFileId(String googleFileId);

    boolean existsByNameAndFolder(String name, DriveFolder folder);

    Optional<DriveFile> findByNameAndFolder(String name, DriveFolder folder);
}

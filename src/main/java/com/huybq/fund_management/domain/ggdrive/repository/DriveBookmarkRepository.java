package com.huybq.fund_management.domain.ggdrive.repository;

import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveBookmarkRepository extends JpaRepository<DriveBookmark, Long> {

    List<DriveBookmark> findByUserId(Long userId);

    List<DriveBookmark> findByUserIdAndCategory(Long userId, String category);
}

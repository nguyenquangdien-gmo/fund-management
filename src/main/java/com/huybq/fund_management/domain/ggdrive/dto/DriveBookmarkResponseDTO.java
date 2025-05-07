package com.huybq.fund_management.domain.ggdrive.dto;

import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveBookmarkResponseDTO {

    private Long id;
    private String name;
    private String url;
    private String googleId;
    private DriveBookmark.BookmarkType type;
    private DriveBookmark.BookmarkSource source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional explicit getter/setter methods to avoid lombok issues
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public DriveBookmark.BookmarkType getType() {
        return type;
    }

    public void setType(DriveBookmark.BookmarkType type) {
        this.type = type;
    }

    public DriveBookmark.BookmarkSource getSource() {
        return source;
    }

    public void setSource(DriveBookmark.BookmarkSource source) {
        this.source = source;
    }

    // Helper methods for bookmark URLs
    public String getWebViewUrl() {
        if (googleId != null && !googleId.isEmpty()) {
            if (type == DriveBookmark.BookmarkType.FILE) {
                return "https://drive.google.com/file/d/" + googleId + "/view";
            } else if (type == DriveBookmark.BookmarkType.FOLDER) {
                return "https://drive.google.com/drive/folders/" + googleId;
            }
        }
        return url;
    }
}

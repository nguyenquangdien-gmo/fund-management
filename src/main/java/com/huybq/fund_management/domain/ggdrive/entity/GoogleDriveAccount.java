package com.huybq.fund_management.domain.ggdrive.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class GoogleDriveAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiryDate;
    private String rootFolderId;
    private String userId;

    @OneToMany(mappedBy = "googleDriveAccount", cascade = CascadeType.ALL)
    private List<DriveFolder> folders = new ArrayList<>();

    @OneToMany(mappedBy = "googleDriveAccount", cascade = CascadeType.ALL)
    private List<DriveFile> files = new ArrayList<>();
}

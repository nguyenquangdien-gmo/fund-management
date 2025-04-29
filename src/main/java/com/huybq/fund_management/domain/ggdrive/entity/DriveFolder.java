package com.huybq.fund_management.domain.ggdrive.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class DriveFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String googleFolderId;
    private String webViewLink;

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private DriveFolder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
    private List<DriveFolder> subFolders = new ArrayList<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL)
    private List<DriveFile> files = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "drive_account_id")
    private GoogleDriveAccount googleDriveAccount;
}

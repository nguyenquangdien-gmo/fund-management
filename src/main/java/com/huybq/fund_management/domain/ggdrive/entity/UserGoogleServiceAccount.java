package com.huybq.fund_management.domain.ggdrive.entity;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_google_service_accounts")
public class UserGoogleServiceAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "description")
    private String description;

    @Column(name = "credentials_file_path")
    private String credentialsFilePath;

    @Column(name = "application_name")
    private String applicationName;

    @Column(name = "root_folder_id")
    private String rootFolderId;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "connection_status")
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus = ConnectionStatus.PENDING;

    @Column(name = "last_connection_check")
    private LocalDateTime lastConnectionCheck;

    @Column(name = "connection_error")
    private String connectionError;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ConnectionStatus {
        PENDING,
        CONNECTED,
        FAILED
    }
}

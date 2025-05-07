package com.huybq.fund_management.domain.ggdrive.dto;

import com.huybq.fund_management.domain.ggdrive.entity.UserGoogleServiceAccount.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleServiceAccountResponseDTO {

    private Long id;
    private String accountName;
    private String description;
    private String applicationName;
    private String rootFolderId;
    private Boolean isDefault;
    private Boolean isActive;
    private ConnectionStatus connectionStatus;
    private LocalDateTime lastConnectionCheck;
    private String connectionError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

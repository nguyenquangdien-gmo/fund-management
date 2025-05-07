package com.huybq.fund_management.domain.ggdrive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleServiceAccountRequestDTO {

    @NotBlank(message = "Account name cannot be blank")
    private String accountName;

    private String description;

    @NotBlank(message = "Application name cannot be blank")
    private String applicationName;

    private String rootFolderId;

    private Boolean isDefault = false;
}

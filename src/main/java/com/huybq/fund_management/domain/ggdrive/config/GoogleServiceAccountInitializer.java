package com.huybq.fund_management.domain.ggdrive.config;

import com.huybq.fund_management.domain.ggdrive.service.GoogleServiceAccountStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleServiceAccountInitializer {

    private final GoogleServiceAccountStorageService storageService;

    /**
     * Initialize services after application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeServices() {
        log.info("Initializing Google Service Account storage location");
        storageService.init();
    }
}

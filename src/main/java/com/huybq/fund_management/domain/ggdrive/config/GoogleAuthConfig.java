package com.huybq.fund_management.domain.ggdrive.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
public class GoogleAuthConfig {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthConfig.class);

    private static final List<String> SCOPES = List.of(
            DriveScopes.DRIVE,     // quyền truy cập Drive
            "openid",              // để lấy id_token
            "email",               // để lấy email
            "profile"              // để lấy name/avatar
    );

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Bean
    public GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() throws IOException {
        try {
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                    .setWeb(new GoogleClientSecrets.Details()
                            .setClientId(clientId)
                            .setClientSecret(clientSecret)
                            .setRedirectUris(List.of(redirectUri)));

            // Create tokens directory if it doesn't exist
            File tokensDirectory = new File(System.getProperty("user.dir"), "tokens");
            if (!tokensDirectory.exists()) {
                boolean created = tokensDirectory.mkdirs();
                if (!created) {
                    logger.warn("Could not create tokens directory, using in-memory store instead");
                    // Use MemoryDataStoreFactory as fallback
                    return new GoogleAuthorizationCodeFlow.Builder(
                            new NetHttpTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                            .setDataStoreFactory(new MemoryDataStoreFactory())
                            .setAccessType("offline")
                            .build();
                }
            }

            return new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory))
                    .setAccessType("offline")
                    .build();
        } catch (Exception e) {
            // Log the actual error to help with debugging
            logger.error("Error initializing GoogleAuthorizationCodeFlow: ", e);
            throw e;
        }
    }
}

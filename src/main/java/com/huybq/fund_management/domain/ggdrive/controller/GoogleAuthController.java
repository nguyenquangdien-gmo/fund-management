package com.huybq.fund_management.domain.ggdrive.controller;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.huybq.fund_management.domain.ggdrive.entity.GoogleDriveAccount;
import com.huybq.fund_management.domain.ggdrive.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/${server.version}/google-auth")
@RequiredArgsConstructor
public class GoogleAuthController {
    private final GoogleAuthorizationCodeFlow flow;
    private final GoogleDriveService driveService;

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @GetMapping("/authorize")
    public RedirectView authorize() {
        String url = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
        return new RedirectView(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<GoogleDriveAccount> callback(@RequestParam("code") String code) throws IOException {
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                transport,
                gsonFactory,
                clientId,
                clientSecret,
                code,
                redirectUri
        ).execute();

        // ⚡ Thêm kiểm tra ID Token
        String idTokenString = tokenResponse.getIdToken();
        if (idTokenString == null) {
            throw new IllegalStateException("ID Token is null. Make sure 'openid email profile' scopes are requested.");
        }

        GoogleIdToken idToken = GoogleIdToken.parse(gsonFactory, idTokenString);
        GoogleIdToken.Payload payload = idToken.getPayload();

        String userId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        GoogleDriveAccount account = new GoogleDriveAccount();
        account.setName(name);
        account.setEmail(email);
        account.setAccessToken(tokenResponse.getAccessToken());
        account.setRefreshToken(tokenResponse.getRefreshToken());
        account.setTokenExpiryDate(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
        account.setRootFolderId("root");

        GoogleDriveAccount savedAccount = driveService.saveGoogleDriveAccount(account);

        try {
            driveService.refreshDriveContents(savedAccount.getId());
        } catch (Exception e) {
            System.err.println("Failed to refresh drive contents: " + e.getMessage());
        }

        return ResponseEntity.ok(savedAccount);
    }

}

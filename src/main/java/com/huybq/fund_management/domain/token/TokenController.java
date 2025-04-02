package com.huybq.fund_management.domain.token;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class TokenController {
    private final JwtService service;

    @GetMapping("/is-admin")
    public ResponseEntity<Boolean>isAdmin(@RequestParam String token) {
        return ResponseEntity.ok(service.isAdmin(token));
    }
    @GetMapping("/expired")
    public ResponseEntity<Boolean>isExpired(@RequestParam String token) {
        return ResponseEntity.ok(service.isTokenValidAndNotExpired(token));
    }

}

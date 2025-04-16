package com.huybq.fund_management.auth;

import com.huybq.fund_management.domain.user.UserDTO;
import com.huybq.fund_management.domain.user.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterDto request,
            Principal principal
    ) {
        String emailLoggedInUser = principal.getName();//get email of logged in

        return ResponseEntity.ok(service.register(request, emailLoggedInUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationDto request) {

        return ResponseEntity.ok(service.authenticate(request));
    }
    @GetMapping("/current-user")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            UserDTO userDto = UserDTO.builder()
                    .email(user.getUsername())
                    .fullName(user.getUsername())
                    .role(user.getAuthorities().stream().findFirst().get().getAuthority())
                    .build();
            return ResponseEntity.ok(userDto);
        } else {
            throw new IllegalStateException("Principal is not an instance of UserDetails: " + principal.getClass());
        }
    }
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        service.logout(request, response,authentication);
    }
    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto request) {
        service.changePassword(request.email(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request, Principal principal) {
        String email = request.get("email");
        service.resetPassword(email, principal.getName());
        return ResponseEntity.ok("Password changed successfully");
    }


}

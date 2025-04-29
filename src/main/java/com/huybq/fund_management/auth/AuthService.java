package com.huybq.fund_management.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huybq.fund_management.domain.chatopsApi.ChatopsService;
import com.huybq.fund_management.domain.role.RoleRepository;
import com.huybq.fund_management.domain.team.TeamRepository;
import com.huybq.fund_management.domain.token.JwtService;
import com.huybq.fund_management.domain.token.Token;
import com.huybq.fund_management.domain.token.TokenRepository;
import com.huybq.fund_management.domain.token.TokenType;
import com.huybq.fund_management.domain.user.*;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${server.domain.url-v1}")
    private String url;
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final Notification notification;
    private final ChatopsService chatopsService;
    private final UserMapper mapper;


    public AuthenticationResponse register(RegisterDto request,String emailAdmin) {
        var team = teamRepository.findBySlug(request.slugTeam().toLowerCase());
        if (team.isEmpty()) {
            throw new ResourceNotFoundException("Team not found");
        }
        var role = roleRepository.findByName(request.role().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        LocalDate dob = parseDate(request.dob(), "Invalid date format for date of birth");
        LocalDate joinDate = parseDate(request.joinDate(), "Invalid date format for join date");

        // Tạo mật khẩu tự động
        String generatedPassword = generatePassword(request.email());
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        Map<String, Object> chatUser = chatopsService.getUserByEmail(request.email());
        String userIdChat = (String) chatUser.get("id");

        var user = User.builder()
                .id(request.id())
                .fullName(request.fullName().toUpperCase())
                .email(request.email())
                .password(encodedPassword)
                .role(role)
                .phone(request.phoneNumber())
                .position(request.position())
                .team(team.get())
                .dob(dob)
                .joinDate(joinDate)
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .userIdChat(userIdChat)
                .build();

        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);

        notification.sendNotificationForMember(
                "Bạn đã được thêm vào Team Java, hãy login và " +
                "đổi mật khẩu\nLink: " +url+"/login"+
                "\nAccount: "+user.getEmail()+"\nPassword: " +
                generatedPassword,emailAdmin,user.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .user(mapper.toResponseDTO(user))
                .build();
    }

    private LocalDate parseDate(String dateStr, String errorMessage) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(errorMessage);
            }
        }
        return null;
    }

    private String generatePassword(String email) {
        String prefix = email.split("@")[0];
        SecureRandom random = new SecureRandom();
        int randomNumber = 10000 + random.nextInt(90000);
        return prefix + randomNumber;
    }

    public void resetPassword(String email,String emailAdmin) {
        System.out.println("Email to reset: " + email);
        System.out.println("Admin who reset: " + emailAdmin);

        var user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String generatedPassword = generatePassword(email);

        user.setPassword(passwordEncoder.encode(generatedPassword));
        repository.save(user);

        notification.sendNotificationForMember(
                "Tài khoản của bạn đã được reset, hãy login và " +
                        "đổi mật khẩu\nLink: " +url+"/change-password"+
                        "\nAccount: "+user.getEmail()+"\nPassword: " +
                        generatedPassword,emailAdmin,user.getEmail());
    }

    public AuthenticationResponse authenticate(AuthenticationDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = repository.findByEmail(request.email())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        var userDto = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName().toUpperCase())
                .role(user.getRole().getName())
                .phoneNumber(user.getPhone())
                .position(user.getPosition())
                .slugTeam(user.getTeam().getName())
                .dob(user.getDob().toString())
                .joinDate(user.getJoinDate().toString())
                .build();

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .user(mapper.toResponseDTO(user))
                .build();
    }
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    public void refreshToken(HttpServletRequest request,HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }




//    public List<UserDto> getAllUser() {
//        List<User> users = repository.findAll();
//        List<UserDto> userDtos = new ArrayList<>();
//        for (User user : users) {
//            UserDto userDto = UserDto.builder()
//                    .email(user.getEmail())
//                    .fullName(user.getFullName())
//                    .role(String.valueOf(user.getRole()))
//                    .build();
//            userDtos.add(userDto);
//        }
//        return userDtos;
//    }

}

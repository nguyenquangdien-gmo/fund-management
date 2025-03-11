package com.huybq.fund_management.domain.user.service;

import com.huybq.fund_management.domain.token.JwtService;
import com.huybq.fund_management.domain.token.Token;
import com.huybq.fund_management.domain.token.TokenRepository;
import com.huybq.fund_management.domain.token.TokenType;
import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.entity.Roles;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.domain.user.response.AuthenticationResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public List<User> getUsers() {
        return repository.findAll();
    }
    public User getUserByEmail(String email) {
        Optional<User> user = repository.findByEmail(email);
        return user.orElse(null);
    }
    @Transactional
    public boolean deleteUserById(Long id) {
        var user = repository.findById(id);

        if (user.isPresent()) {
            tokenRepository.deleteTokenByUser_Id(id);
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
    public AuthenticationResponse updateUserById(Long id, UserDto u) {
        User user = repository.findById(id).orElse(null);
        if (user != null) {
            user.setId(id);
            user.setFullName(u.fullName());
            user.setEmail(u.email());
            user.setRole(Roles.valueOf(u.role()));
            user.setUpdatedAt(LocalDateTime.now());
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            repository.save(user);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        return null;
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
    @Transactional
    public void deleteMultiple(List<Long> ids) {
        for (Long id : ids) {
            tokenRepository.deleteTokenByUser_Id(id);
            repository.deleteById(id);
            repository.findById(id).ifPresent(product -> repository.deleteById(id));
        }
    }

}

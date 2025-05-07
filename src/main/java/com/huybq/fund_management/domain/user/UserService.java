package com.huybq.fund_management.domain.user;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.domain.reminder.ReminderRepository;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUser;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUserRepository;
import com.huybq.fund_management.domain.role.Role;
import com.huybq.fund_management.domain.role.RoleRepository;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamRepository;
import com.huybq.fund_management.domain.token.JwtService;
import com.huybq.fund_management.domain.token.Token;
import com.huybq.fund_management.domain.token.TokenRepository;
import com.huybq.fund_management.domain.token.TokenType;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final UserMapper mapper;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final ReminderRepository reminderRepository;
    private final UserMapper userMapper;

    public List<UserResponseDTO> getUsers() {
        return repository.findAllByIsDeleteIsFalse().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getUsersExcludeCurrent() {
        // Lấy username từ security context
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy toàn bộ user ngoại trừ user đã bị xóa và user hiện tại
        return repository.findAllByIsDeleteIsFalseAndEmailNot(currentUsername).stream()
                .map(mapper::toResponseDTO).toList();
    }

    public boolean existByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public void updateUser(Long userId, UserDTO userDTO, MultipartFile avatar, MultipartFile qrCode) throws IOException {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDTO.id()));
//        user.setId(userDTO.id());
        user.setEmail(userDTO.email());
        user.setFullName(userDTO.fullName());
        user.setPhone(userDTO.phoneNumber()!=null?userDTO.phoneNumber():user.getPhone());
        user.setPosition(userDTO.position()!=null?userDTO.position():user.getPosition());
        user.setDob(LocalDate.parse(userDTO.dob()));
        user.setJoinDate(LocalDate.parse(userDTO.joinDate()));

        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar.getBytes());
        }

        if (qrCode != null && !qrCode.isEmpty()) {
            user.setQrCode(qrCode.getBytes());
        }

        repository.save(user);
    }

    public byte[] getAvatar(Long userId) {
        return getUserField(userId, User::getAvatar);
    }

    public byte[] getQRcode(Long userId) {
        return getUserField(userId, User::getQrCode);
    }

    private byte[] getUserField(Long userId, Function<User, byte[]> fieldExtractor) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return fieldExtractor.apply(user);
    }
//    public List<Reminder> findRemindersByUserId(Long userId) {
//        User user = repository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
//
//        return user.getReminderUsers().stream()
//                .map(ReminderUser::getReminder)
//                .sorted(Comparator.comparing(Reminder::getCreatedAt).reversed())
//                .limit(10)
//                .collect(Collectors.toList());
//    }


    public List<UserDebtDTO> getUsersWithNoContribution(int month, int year) {
        return repository.findUsersWithNoContribution(month, year);
    }

    public UserResponseDTO getUserByEmail(String email) {
        Optional<User> user = repository.findByEmail(email);
        return mapper.toResponseDTO(user.get());
    }

    @Transactional
    public boolean deleteUserById(Long id) {
        var user = repository.findById(id);

        if (user.isPresent()) {
            user.get().setDelete(true);
            user.get().setStatus(Status.INACTIVE);
            repository.save(user.get());
            return true;
        } else {
            return false;
        }
    }

    public AuthenticationResponse updateUserById(Long id, UserDTO u) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        Team team = teamRepository.findBySlug(u.slugTeam()).orElseThrow(() -> new EntityNotFoundException("Team is not found with slug: " + u.slugTeam()));

        Role role = roleRepository.findByName(u.role()).orElseThrow(() -> new EntityNotFoundException("Role is not found with name: " + u.role()));
        if (user != null) {
            user.setId(id);
            user.setFullName(u.fullName());
            user.setEmail(u.email());
            user.setRole(role);
            user.setPhone(u.phoneNumber());
            user.setPosition(u.position());
            user.setTeam(team);
            user.setJoinDate(LocalDate.parse(u.joinDate()));
            user.setDob(LocalDate.parse(u.dob()));

            var jwtToken = jwtService.generateToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            repository.save(user);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .user(mapper.toResponseDTO(user))
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

    public List<UserLatePaymentDTO> getLatePayments(int month, int year) {
        return repository.findUsersWithLatePayment(month, year);
    }

    public Team getTeamByUserId(Long userId) {
        return repository.findByIdAndIsDeleteIsFalse(userId)
                .map(User::getTeam)
                .orElseThrow(() -> new RuntimeException("User không tồn tại hoặc đã bị xóa"));
    }

    public List<UserResponseDTO> getMembersByTeamSlug(String slug) {
        Team team = teamRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
        return repository.findAllByTeamAndIsDeleteIsFalse(team).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}

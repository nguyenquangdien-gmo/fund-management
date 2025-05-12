package com.huybq.fund_management.domain.ggdrive.repository;

import com.huybq.fund_management.domain.ggdrive.entity.UserGoogleServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGoogleServiceAccountRepository extends JpaRepository<UserGoogleServiceAccount, Long> {

    // Lấy danh sách tất cả tài khoản của một user
    List<UserGoogleServiceAccount> findByUserId(Long userId);

    // Lấy danh sách các tài khoản đang active của một user
    List<UserGoogleServiceAccount> findByIsActiveTrue();

    // Lấy danh sách tài khoản ngoại trừ một tài khoản cụ thể
    List<UserGoogleServiceAccount> findByIdNot(Long id);

    // Lấy danh sách tài khoản active ngoại trừ một tài khoản cụ thể
    List<UserGoogleServiceAccount> findByIdNotAndIsActiveTrue(Long id);

    // Lấy tài khoản mặc định của user
    Optional<UserGoogleServiceAccount> findByIsDefaultTrue();

    // Lấy tài khoản mặc định đang active của user
    Optional<UserGoogleServiceAccount> findByIsDefaultTrueAndIsActiveTrue();

    boolean existsByAccountName(String accountName);

    // Kiểm tra có tồn tại tài khoản mặc định không
    boolean existsByIsDefaultTrue();
}

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
    List<UserGoogleServiceAccount> findByUserIdAndIsActiveTrue(Long userId);

    // Lấy danh sách tài khoản ngoại trừ một tài khoản cụ thể
    List<UserGoogleServiceAccount> findByUserIdAndIdNot(Long userId, Long id);

    // Lấy danh sách tài khoản active ngoại trừ một tài khoản cụ thể
    List<UserGoogleServiceAccount> findByUserIdAndIdNotAndIsActiveTrue(Long userId, Long id);

    // Lấy tài khoản mặc định của user
    Optional<UserGoogleServiceAccount> findByUserIdAndIsDefaultTrue(Long userId);

    // Lấy tài khoản mặc định đang active của user
    Optional<UserGoogleServiceAccount> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);

    // Tìm tài khoản theo ID và userID (để đảm bảo người dùng chỉ truy cập tài khoản của họ)
    Optional<UserGoogleServiceAccount> findByIdAndUserId(Long id, Long userId);

    // Kiểm tra tên tài khoản đã tồn tại cho user chưa
    boolean existsByUserIdAndAccountName(Long userId, String accountName);

    // Kiểm tra có tồn tại tài khoản mặc định không
    boolean existsByUserIdAndIsDefaultTrue(Long userId);
}

package com.huybq.fund_management.domain.user.repository;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.user.dto.UserDebtDTO;
import com.huybq.fund_management.domain.user.dto.UserLatePaymentDTO;
import com.huybq.fund_management.domain.user.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByFullName(String fullName);

    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT c.user.id FROM Contribution c WHERE c.period.month = :month AND c.period.year = :year " +
            "AND c.paymentStatus = 'PAID')")
    List<User> findUsersOwedContributed(@Param("month") int month, @Param("year") int year);

    @Query("SELECT new com.huybq.fund_management.domain.user.dto.UserDebtDTO(" +
            "u,CAST(p.totalAmount AS BigDecimal)) " +
            "FROM User u " +
            "LEFT JOIN Contribution c ON c.user = u AND c.period.month = :month AND c.period.year = :year " +
            "JOIN Period p ON p.month = :month AND p.year = :year " +
            "WHERE c.id IS NULL")
    List<UserDebtDTO> findUsersWithNoContribution(@Param("month") int month, @Param("year") int year);

    @Query("SELECT new com.huybq.fund_management.domain.user.dto.UserLatePaymentDTO(" +
            "u, c.totalAmount, c.createdAt) " +
            "FROM User u " +
            "JOIN Contribution c ON c.user = u " +
            "JOIN Period p ON c.period = p " +
            "WHERE p.month = :month AND p.year = :year " +
            "AND (c.isLate = true OR c.paymentStatus = 'LATE')")
    List<UserLatePaymentDTO> findUsersWithLatePayment(int month, int year);
    @Query("""
           \s
            Select u from User u where u.isDelete = false\s
           \s""")
    List<User> findAllByIsDeleteIsFalse();

    Optional<User> findByIdAndIsDeleteIsFalse(Long id);

}

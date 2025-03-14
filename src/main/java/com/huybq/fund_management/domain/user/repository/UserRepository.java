package com.huybq.fund_management.domain.user.repository;

import com.huybq.fund_management.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT c.user.id FROM Contribution c WHERE c.period.month = :month AND c.period.year = :year " +
            "AND c.paymentStatus = 'PAID')")
    List<User> findUsersNotFullyContributed(@Param("month") int month, @Param("year") int year);

}

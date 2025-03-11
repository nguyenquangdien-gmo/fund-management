package com.huybq.fund_management.user.repository;

import com.huybq.fund_management.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<Object> findByEmail(String username);
}

package com.huybq.fund_management.domain.penalty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    Optional<Penalty> findByName(String name);
}

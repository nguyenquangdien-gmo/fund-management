package com.huybq.fund_management.domain.late;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LateRepository extends JpaRepository<Late, Long> {
    List<Late> findByDate(LocalDate date);
}

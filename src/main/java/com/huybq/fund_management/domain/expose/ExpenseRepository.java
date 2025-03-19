package com.huybq.fund_management.domain.expose;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExposeRepository extends JpaRepository<Expose, Long> {
    List<Expose> findByUserId(Long userId);
}

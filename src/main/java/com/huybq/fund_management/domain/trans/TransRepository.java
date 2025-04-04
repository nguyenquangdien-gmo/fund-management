package com.huybq.fund_management.domain.trans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransRepository extends JpaRepository<Trans, Long> {
    List<Trans> findAllByOrderByCreatedAtDesc();
}

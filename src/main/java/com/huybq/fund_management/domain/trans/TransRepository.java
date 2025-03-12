package com.huybq.fund_management.domain.trans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransRepository extends JpaRepository<Trans, Long> {
}

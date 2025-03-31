package com.huybq.fund_management.domain.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team,Integer> {
    List<Team> findUsersByName(String name);
    Optional<Team> findBySlug(String slug);

//    Optional<Team> findTeamByMemberId(Long memberId);
}

package com.huybq.fund_management.domain.event;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByNameContaining(String name);

    @EntityGraph(attributePaths = "hosts")
    List<Event> findByEventTimeBetween(LocalDateTime from, LocalDateTime to);


    List<Event> findAllByEventTimeGreaterThanEqual(LocalDateTime eventTime);


}

package com.eventhub.repository;

import com.eventhub.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByEventId(Long eventId);
    Optional<Team> findByEventIdAndName(Long eventId, String name);
    boolean existsByEventIdAndName(Long eventId, String name);
    Optional<Team> findByEventIdAndLeaderId(Long eventId, Long leaderId);
}

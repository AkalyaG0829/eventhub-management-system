package com.eventhub.repository;

import com.eventhub.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByEventId(Long eventId);
    Optional<Submission> findByTeamIdAndEventId(Long teamId, Long eventId);
    boolean existsByTeamId(Long teamId);
    Optional<Submission> findByTeamId(Long teamId);
}

package com.eventhub.repository;

import com.eventhub.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByParticipantId(Long participantId);
    Optional<Registration> findByParticipantIdAndEventId(Long participantId, Long eventId);
    boolean existsByParticipantIdAndEventId(Long participantId, Long eventId);
    long countByEventId(Long eventId);
}

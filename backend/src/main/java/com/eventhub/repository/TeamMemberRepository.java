package com.eventhub.repository;

import com.eventhub.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamId(Long teamId);
    List<TeamMember> findByUserId(Long userId);
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    boolean existsByTeam_Event_IdAndUserId(Long eventId, Long userId);
}

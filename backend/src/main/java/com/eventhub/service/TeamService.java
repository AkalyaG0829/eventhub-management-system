package com.eventhub.service;

import com.eventhub.dto.TeamMemberResponse;
import com.eventhub.dto.TeamRequest;
import com.eventhub.dto.TeamResponse;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.*;
import com.eventhub.repository.*;
import com.eventhub.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Transactional
    public TeamResponse createTeam(Long eventId, TeamRequest request, CustomUserDetails currentUser) {
        User leader = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getParticipationType() != com.eventhub.model.ParticipationType.TEAM) {
            throw new com.eventhub.exception.BadRequestException("This event does not allow team participation");
        }

        if (!registrationRepository.existsByParticipantIdAndEventId(leader.getId(), event.getId())) {
            throw new AccessDeniedException("You must be registered for the event to create a team");
        }

        if (teamRepository.existsByEventIdAndName(eventId, request.getName())) {
            throw new ConflictException("Team name already exists for this event");
        }

        if (teamMemberRepository.existsByTeam_Event_IdAndUserId(eventId, leader.getId())) {
            throw new ConflictException("You are already part of a team for this event");
        }

        Team team = Team.builder()
                .name(request.getName())
                .event(event)
                .leader(leader)
                .build();

        Team savedTeam = teamRepository.save(team);

        TeamMember leaderMember = TeamMember.builder()
                .team(savedTeam)
                .user(leader)
                .role(TeamRole.LEADER)
                .build();

        teamMemberRepository.save(leaderMember);
        savedTeam.getMembers().add(leaderMember);

        return mapToDto(savedTeam);
    }

    public TeamResponse getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        return mapToDto(team);
    }

    public List<TeamResponse> getTeamsByEvent(Long eventId, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view teams for this event");
        }

        List<Team> teams = teamRepository.findByEventId(eventId);
        return teams.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<TeamResponse> getMyTeams(CustomUserDetails currentUser) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(currentUser.getId());
        return memberships.stream().map(m -> mapToDto(m.getTeam())).collect(Collectors.toList());
    }

    @Transactional
    public TeamResponse addTeamMember(Long teamId, CustomUserDetails currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        User newMember = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = team.getEvent();

        if (!registrationRepository.existsByParticipantIdAndEventId(newMember.getId(), event.getId())) {
            throw new AccessDeniedException("You must be registered for the event to join a team");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, newMember.getId())) {
            throw new ConflictException("You are already a member of this team");
        }

        if (teamMemberRepository.existsByTeam_Event_IdAndUserId(event.getId(), newMember.getId())) {
            throw new ConflictException("You are already part of another team for this event");
        }

        long currentMembers = teamMemberRepository.findByTeamId(teamId).size();
        if (event.getTeamSizeMax() != null && currentMembers >= event.getTeamSizeMax()) {
            throw new ConflictException("Team has reached its maximum size of " + event.getTeamSizeMax());
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(newMember)
                .role(TeamRole.MEMBER)
                .build();

        teamMemberRepository.save(member);
        team.getMembers().add(member);

        return mapToDto(team);
    }

    @Transactional
    public void leaveTeam(Long teamId, CustomUserDetails currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        TeamMember member = teamMemberRepository.findByTeamId(teamId).stream()
                .filter(m -> m.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this team"));

        if (member.getRole() == TeamRole.LEADER) {
            throw new ConflictException("Team leader cannot leave the team. Transfer leadership or delete the team.");
        }

        teamMemberRepository.delete(member);
    }

    @Transactional
    public void removeTeamMember(Long teamId, Long userId, CustomUserDetails currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (!team.getLeader().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the team leader can remove members");
        }

        TeamMember member = teamMemberRepository.findByTeamId(teamId).stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this team"));

        if (member.getRole() == TeamRole.LEADER) {
            throw new ConflictException("Cannot remove the team leader");
        }

        teamMemberRepository.delete(member);
    }

    private TeamResponse mapToDto(Team team) {
        List<TeamMemberResponse> memberResponses = team.getMembers().stream()
                .map(m -> TeamMemberResponse.builder()
                        .id(m.getId())
                        .teamId(m.getTeam().getId())
                        .userId(m.getUser().getId())
                        .userName(m.getUser().getName())
                        .role(m.getRole())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .eventId(team.getEvent().getId())
                .eventTitle(team.getEvent().getTitle())
                .leaderId(team.getLeader().getId())
                .leaderName(team.getLeader().getName())
                .members(memberResponses)
                .createdAt(team.getCreatedAt())
                .build();
    }
}

package com.eventhub.service;

import com.eventhub.dto.SubmissionRequest;
import com.eventhub.dto.SubmissionResponse;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.Event;
import com.eventhub.model.EventStatus;
import com.eventhub.model.Submission;
import com.eventhub.model.SubmissionStatus;
import com.eventhub.model.Team;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.SubmissionRepository;
import com.eventhub.repository.TeamRepository;
import com.eventhub.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private com.eventhub.repository.TeamMemberRepository teamMemberRepository;

    public SubmissionResponse createSubmission(Long teamId, SubmissionRequest request, CustomUserDetails currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (!team.getLeader().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the team leader can submit the project");
        }

        Event event = team.getEvent();

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("Cannot submit for a cancelled event");
        }

        if (LocalDateTime.now().isAfter(event.getEndDate())) {
            throw new ConflictException("The submission deadline has passed");
        }

        if (submissionRepository.existsByTeamId(teamId)) {
            throw new ConflictException("This team has already submitted a project");
        }

        Submission submission = Submission.builder()
                .team(team)
                .event(event)
                .projectTitle(request.getProjectTitle())
                .description(request.getDescription())
                .repositoryUrl(request.getRepositoryUrl())
                .demoUrl(request.getDemoUrl())
                .status(SubmissionStatus.SUBMITTED)
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        return mapToDto(savedSubmission);
    }

    public SubmissionResponse updateSubmission(Long id, SubmissionRequest request, CustomUserDetails currentUser) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        Team team = submission.getTeam();

        if (!team.getLeader().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the team leader can update the project submission");
        }

        if (LocalDateTime.now().isAfter(team.getEvent().getEndDate())) {
            throw new ConflictException("The submission deadline has passed");
        }

        submission.setProjectTitle(request.getProjectTitle());
        submission.setDescription(request.getDescription());
        submission.setRepositoryUrl(request.getRepositoryUrl());
        submission.setDemoUrl(request.getDemoUrl());

        Submission updatedSubmission = submissionRepository.save(submission);
        return mapToDto(updatedSubmission);
    }

    public void deleteSubmission(Long id, CustomUserDetails currentUser) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        Team team = submission.getTeam();

        if (!team.getLeader().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the team leader can delete the project submission");
        }

        if (LocalDateTime.now().isAfter(team.getEvent().getEndDate())) {
            throw new ConflictException("Cannot delete submission after the event deadline");
        }

        submissionRepository.delete(submission);
    }

    public SubmissionResponse getSubmissionById(Long id, CustomUserDetails currentUser) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        checkSubmissionAccess(submission, currentUser);

        return mapToDto(submission);
    }

    public List<SubmissionResponse> getSubmissionsByTeam(Long teamId, CustomUserDetails currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOrganizer = team.getEvent().getOrganizer().getId().equals(currentUser.getId());
        boolean isTeamMember = team.getMembers().stream().anyMatch(m -> m.getUser().getId().equals(currentUser.getId()));

        if (!isAdmin && !isOrganizer && !isTeamMember) {
            throw new AccessDeniedException("You do not have permission to view submissions for this team");
        }

        return submissionRepository.findByTeamId(teamId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponse> getMySubmissions(CustomUserDetails currentUser) {
        List<com.eventhub.model.TeamMember> memberships = teamMemberRepository.findByUserId(currentUser.getId());
        return memberships.stream()
                .map(m -> submissionRepository.findByTeamId(m.getTeam().getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void checkSubmissionAccess(Submission submission, CustomUserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOrganizer = submission.getEvent().getOrganizer().getId().equals(currentUser.getId());
        boolean isTeamMember = submission.getTeam().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()));

        if (!isAdmin && !isOrganizer && !isTeamMember) {
            throw new AccessDeniedException("You do not have permission to view this submission");
        }
    }

    private SubmissionResponse mapToDto(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .teamId(submission.getTeam().getId())
                .teamName(submission.getTeam().getName())
                .eventId(submission.getEvent().getId())
                .projectTitle(submission.getProjectTitle())
                .description(submission.getDescription())
                .repositoryUrl(submission.getRepositoryUrl())
                .demoUrl(submission.getDemoUrl())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}

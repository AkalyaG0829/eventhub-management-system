package com.eventhub.service;

import com.eventhub.dto.LeaderboardEntryResponse;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.Event;
import com.eventhub.model.Evaluation;
import com.eventhub.model.Submission;
import com.eventhub.model.SubmissionStatus;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.EvaluationRepository;
import com.eventhub.repository.SubmissionRepository;
import com.eventhub.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    public List<LeaderboardEntryResponse> getLeaderboard(Long eventId, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOrganizer = currentUser != null && event.getOrganizer().getId().equals(currentUser.getId());

        if (!event.isResultsPublished() && !isAdmin && !isOrganizer) {
            throw new AccessDeniedException("The results for this event have not been published yet");
        }

        List<Submission> submissions = submissionRepository.findByEventId(eventId).stream()
                .filter(s -> s.getStatus() == SubmissionStatus.EVALUATED)
                .collect(Collectors.toList());

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();

        for (Submission submission : submissions) {
            List<Evaluation> evaluations = evaluationRepository.findBySubmissionId(submission.getId());
            if (evaluations.isEmpty()) continue;

            BigDecimal avgScore = evaluations.stream()
                    .map(Evaluation::getTotalScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);

            leaderboard.add(LeaderboardEntryResponse.builder()
                    .teamId(submission.getTeam().getId())
                    .teamName(submission.getTeam().getName())
                    .submissionId(submission.getId())
                    .projectTitle(submission.getProjectTitle())
                    .totalScore(avgScore)
                    .build());
        }

        leaderboard.sort(Comparator.comparing(LeaderboardEntryResponse::getTotalScore).reversed());

        int rank = 1;
        for (LeaderboardEntryResponse entry : leaderboard) {
            entry.setRank(rank++);
        }

        return leaderboard;
    }

    public void publishResults(Long eventId, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the event organizer or admin can publish the results");
        }

        if (event.isResultsPublished()) {
            throw new ConflictException("Results for this event are already published");
        }

        event.setResultsPublished(true);
        eventRepository.save(event);
    }
}

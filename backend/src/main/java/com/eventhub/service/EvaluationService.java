package com.eventhub.service;

import com.eventhub.dto.EvaluationRequest;
import com.eventhub.dto.EvaluationResponse;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.Evaluation;
import com.eventhub.model.Submission;
import com.eventhub.model.SubmissionStatus;
import com.eventhub.model.User;
import com.eventhub.repository.EvaluationRepository;
import com.eventhub.repository.SubmissionRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public EvaluationResponse evaluateSubmission(Long submissionId, EvaluationRequest request, CustomUserDetails currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        User judge = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Judge user not found"));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !submission.getEvent().getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the event organizer or an admin can evaluate this submission");
        }

        if (evaluationRepository.existsBySubmissionIdAndJudgeId(submissionId, judge.getId())) {
            throw new ConflictException("You have already evaluated this submission");
        }

        BigDecimal totalScore = calculateTotalScore(request);

        Evaluation evaluation = Evaluation.builder()
                .submission(submission)
                .judge(judge)
                .innovationScore(request.getInnovationScore())
                .technicalScore(request.getTechnicalScore())
                .uiUxScore(request.getUiUxScore())
                .impactScore(request.getImpactScore())
                .presentationScore(request.getPresentationScore())
                .totalScore(totalScore)
                .feedback(request.getFeedback())
                .build();

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);

        submission.setStatus(SubmissionStatus.EVALUATED);
        submissionRepository.save(submission);

        return mapToDto(savedEvaluation);
    }

    public List<EvaluationResponse> getEvaluationsForSubmission(Long submissionId, CustomUserDetails currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOrganizer = submission.getEvent().getOrganizer().getId().equals(currentUser.getId());
        
        // Let's only allow organizer, admin, or the team members to view evaluation
        boolean isTeamMember = submission.getTeam().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()));

        if (!isAdmin && !isOrganizer && !isTeamMember) {
            throw new AccessDeniedException("You do not have permission to view these evaluations");
        }

        List<Evaluation> evaluations = evaluationRepository.findBySubmissionId(submissionId);
        return evaluations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private BigDecimal calculateTotalScore(EvaluationRequest request) {
        return request.getInnovationScore()
                .add(request.getTechnicalScore())
                .add(request.getUiUxScore())
                .add(request.getImpactScore())
                .add(request.getPresentationScore());
    }

    private EvaluationResponse mapToDto(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .submissionId(evaluation.getSubmission().getId())
                .judgeId(evaluation.getJudge().getId())
                .judgeName(evaluation.getJudge().getName())
                .innovationScore(evaluation.getInnovationScore())
                .technicalScore(evaluation.getTechnicalScore())
                .uiUxScore(evaluation.getUiUxScore())
                .impactScore(evaluation.getImpactScore())
                .presentationScore(evaluation.getPresentationScore())
                .totalScore(evaluation.getTotalScore())
                .feedback(evaluation.getFeedback())
                .evaluatedAt(evaluation.getEvaluatedAt())
                .build();
    }
}

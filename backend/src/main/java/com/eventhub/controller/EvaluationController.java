package com.eventhub.controller;

import com.eventhub.dto.EvaluationRequest;
import com.eventhub.dto.EvaluationResponse;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Evaluation API", description = "Operations for judging project submissions")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Operation(summary = "Evaluate a submission", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/submissions/{submissionId}/evaluations")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EvaluationResponse> evaluateSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        EvaluationResponse evaluation = evaluationService.evaluateSubmission(submissionId, request, currentUser);
        return new ResponseEntity<>(evaluation, HttpStatus.CREATED);
    }

    @Operation(summary = "Get evaluations for a submission", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/submissions/{submissionId}/evaluations")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsForSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(evaluationService.getEvaluationsForSubmission(submissionId, currentUser));
    }
}

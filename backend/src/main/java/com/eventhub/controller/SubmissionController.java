package com.eventhub.controller;

import com.eventhub.dto.ApiResponse;
import com.eventhub.dto.SubmissionRequest;
import com.eventhub.dto.SubmissionResponse;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.SubmissionService;
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
@Tag(name = "Submission API", description = "Operations for project submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Operation(summary = "Submit a project for a team", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/teams/{teamId}/submissions")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @PathVariable Long teamId,
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        SubmissionResponse submission = submissionService.createSubmission(teamId, request, currentUser);
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @Operation(summary = "Get team's submission", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/teams/{teamId}/submissions")
    @PreAuthorize("hasRole('PARTICIPANT') or hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<SubmissionResponse>> getTeamSubmissions(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(submissionService.getSubmissionsByTeam(teamId, currentUser));
    }

    @Operation(summary = "Get my submissions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/submissions/my")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(submissionService.getMySubmissions(currentUser));
    }

    @Operation(summary = "Get submission by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/submissions/{id}")
    @PreAuthorize("hasRole('PARTICIPANT') or hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(submissionService.getSubmissionById(id, currentUser));
    }

    @Operation(summary = "Update a submission", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/submissions/{id}")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<SubmissionResponse> updateSubmission(
            @PathVariable Long id,
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(submissionService.updateSubmission(id, request, currentUser));
    }

    @Operation(summary = "Delete a submission", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/submissions/{id}")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<ApiResponse> deleteSubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        submissionService.deleteSubmission(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Submission deleted successfully"));
    }
}

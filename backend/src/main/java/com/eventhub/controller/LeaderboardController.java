package com.eventhub.controller;

import com.eventhub.dto.ApiResponse;
import com.eventhub.dto.LeaderboardEntryResponse;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Leaderboard API", description = "Operations for leaderboards and results")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @Operation(summary = "Get event leaderboard", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/events/{eventId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(eventId, currentUser));
    }

    @Operation(summary = "Publish event results", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/events/{eventId}/results/publish")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> publishResults(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        leaderboardService.publishResults(eventId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Results published successfully"));
    }
}

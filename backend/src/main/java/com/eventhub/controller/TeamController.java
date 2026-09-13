package com.eventhub.controller;

import com.eventhub.dto.ApiResponse;
import com.eventhub.dto.TeamRequest;
import com.eventhub.dto.TeamResponse;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.TeamService;
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
@Tag(name = "Team API", description = "Operations for team management")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Operation(summary = "Create a team for an event", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/events/{eventId}/teams")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable Long eventId,
            @Valid @RequestBody TeamRequest teamRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TeamResponse createdTeam = teamService.createTeam(eventId, teamRequest, currentUser);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @Operation(summary = "Get teams for an event", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/events/{eventId}/teams")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<TeamResponse>> getTeamsByEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(teamService.getTeamsByEvent(eventId, currentUser));
    }

    @Operation(summary = "Get my teams", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/teams/my")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<List<TeamResponse>> getMyTeams(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(teamService.getMyTeams(currentUser));
    }

    @Operation(summary = "Get team by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamById(teamId));
    }

    @Operation(summary = "Join a team", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/teams/{teamId}/members")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<TeamResponse> joinTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TeamResponse team = teamService.addTeamMember(teamId, currentUser);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Leave a team", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/teams/{teamId}/members/me")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<ApiResponse> leaveTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        teamService.leaveTeam(teamId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Successfully left the team"));
    }

    @Operation(summary = "Remove a team member (Leader only)", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/teams/{teamId}/members/{userId}")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<ApiResponse> removeTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        teamService.removeTeamMember(teamId, userId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Member removed successfully"));
    }
}

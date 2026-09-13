package com.eventhub.controller;

import com.eventhub.dto.admin.AdminStatsResponse;
import com.eventhub.dto.admin.AdminUserResponse;
import com.eventhub.model.Role;
import com.eventhub.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Operations for global platform administration")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Operation(summary = "Get global platform statistics")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @Operation(summary = "Search platform users")
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserResponse> users = adminService.searchUsers(search, role, enabled, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Deactivate a user account")
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<AdminUserResponse> deactivateUser(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.eventhub.security.CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, false, currentUser));
    }

    @Operation(summary = "Activate a user account")
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<AdminUserResponse> activateUser(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.eventhub.security.CustomUserDetails currentUser) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, true, currentUser));
    }
}

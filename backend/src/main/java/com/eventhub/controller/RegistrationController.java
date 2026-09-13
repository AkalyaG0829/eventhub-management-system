package com.eventhub.controller;

import com.eventhub.dto.RegistrationResponse;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Registration API", description = "Operations for event registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Operation(summary = "Register for an event", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/events/{eventId}/register")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<RegistrationResponse> registerForEvent(
            @PathVariable Long eventId,
            @jakarta.validation.Valid @RequestBody(required = false) com.eventhub.dto.RegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        RegistrationResponse registration = registrationService.registerForEvent(eventId, request, currentUser);
        return new ResponseEntity<>(registration, HttpStatus.CREATED);
    }

    @Operation(summary = "Get my registrations", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/registrations/my")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(registrationService.getMyRegistrations(currentUser));
    }

    @Operation(summary = "Get registrations for an event (Organizer/Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationResponse>> getEventRegistrations(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(registrationService.getEventRegistrations(eventId, currentUser));
    }

    @org.springframework.beans.factory.annotation.Value("${ENABLE_TEST_PAYMENT:false}")
    private boolean enableTestPayment;

    @Operation(summary = "Simulate payment for development", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/events/{eventId}/registrations/{registrationId}/test-payment")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<?> simulatePayment(
            @PathVariable Long eventId,
            @PathVariable Long registrationId,
            @RequestParam boolean success,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!enableTestPayment) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(java.util.Map.of("message", "Test payment simulation is disabled in this environment."));
        }
        RegistrationResponse response = registrationService.simulatePayment(eventId, registrationId, success, currentUser);
        return ResponseEntity.ok(response);
    }
}

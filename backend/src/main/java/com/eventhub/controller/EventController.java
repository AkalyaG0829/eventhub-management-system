package com.eventhub.controller;

import com.eventhub.dto.ApiResponse;
import com.eventhub.dto.EventRequest;
import com.eventhub.dto.EventResponse;
import com.eventhub.model.EventStatus;
import com.eventhub.model.EventType;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event API", description = "Operations for event management and discovery")
public class EventController {

    @Autowired
    private EventService eventService;

    @Operation(summary = "Get all events with filters and pagination")
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) com.eventhub.model.RegistrationType registrationType,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long organizerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> events = eventService.searchEvents(search, type, registrationType, status, location, organizerId, currentUser, pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get an event by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @Operation(summary = "Create a new event", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest,
                                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        EventResponse createdEvent = eventService.createEvent(eventRequest, currentUser);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing event", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
                                                     @Valid @RequestBody EventRequest eventRequest,
                                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        EventResponse updatedEvent = eventService.updateEvent(id, eventRequest, currentUser);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Delete an event", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteEvent(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        eventService.deleteEvent(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Event deleted successfully"));
    }

    @Operation(summary = "Publish a draft event", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        EventResponse publishedEvent = eventService.publishEvent(id, currentUser);
        return ResponseEntity.ok(publishedEvent);
    }
}

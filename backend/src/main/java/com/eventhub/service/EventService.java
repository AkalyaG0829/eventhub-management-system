package com.eventhub.service;

import com.eventhub.dto.EventRequest;
import com.eventhub.dto.EventResponse;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.Event;
import com.eventhub.model.EventStatus;
import com.eventhub.model.EventType;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public EventResponse createEvent(EventRequest request, CustomUserDetails currentUser) {
        User organizer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (organizer.getRole() == Role.PARTICIPANT) {
            throw new AccessDeniedException("Participants cannot create events");
        }

        validateEventDates(request);

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .organizer(organizer)
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .eligibility(request.getEligibility())
                .maxParticipants(request.getMaxParticipants())
                .teamSizeMin(request.getTeamSizeMin())
                .teamSizeMax(request.getTeamSizeMax())
                .registrationFee(request.getRegistrationFee())
                .status(EventStatus.DRAFT)
                .rules(request.getRules())
                .prizes(request.getPrizes())
                .bannerUrl(request.getBannerUrl())
                .participationType(request.getParticipationType())
                .workshopMode(request.getWorkshopMode())
                .instructor(request.getInstructor())
                .capacity(request.getCapacity())
                .workshopLevel(request.getWorkshopLevel())
                .webinarPlatform(request.getWebinarPlatform())
                .webinarUrl(request.getWebinarUrl())
                .registrationType(request.getRegistrationType())
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToDto(savedEvent);
    }

    public EventResponse updateEvent(Long id, EventRequest request, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        checkEventOwnershipOrAdmin(event, currentUser);
        validateEventDates(request);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setLocation(request.getLocation());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setEligibility(request.getEligibility());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setTeamSizeMin(request.getTeamSizeMin());
        event.setTeamSizeMax(request.getTeamSizeMax());
        event.setRegistrationFee(request.getRegistrationFee());
        event.setRules(request.getRules());
        event.setPrizes(request.getPrizes());
        event.setBannerUrl(request.getBannerUrl());
        event.setParticipationType(request.getParticipationType());
        event.setWorkshopMode(request.getWorkshopMode());
        event.setInstructor(request.getInstructor());
        event.setCapacity(request.getCapacity());
        event.setWorkshopLevel(request.getWorkshopLevel());
        event.setWebinarPlatform(request.getWebinarPlatform());
        event.setWebinarUrl(request.getWebinarUrl());
        event.setRegistrationType(request.getRegistrationType());

        Event updatedEvent = eventRepository.save(event);
        return mapToDto(updatedEvent);
    }

    public void deleteEvent(Long id, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        checkEventOwnershipOrAdmin(event, currentUser);
        eventRepository.delete(event);
    }

    public EventResponse publishEvent(Long id, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        checkEventOwnershipOrAdmin(event, currentUser);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event updatedEvent = eventRepository.save(event);
        return mapToDto(updatedEvent);
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToDto(event);
    }

    public Page<EventResponse> searchEvents(String search, EventType type, com.eventhub.model.RegistrationType registrationType, EventStatus status, String location, Long organizerId, CustomUserDetails currentUser, Pageable pageable) {
        Specification<Event> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (registrationType != null) {
                predicates.add(cb.equal(root.get("registrationType"), registrationType));
            }
            if (location != null && !location.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (organizerId != null) {
                predicates.add(cb.equal(root.get("organizer").get("id"), organizerId));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                // Fallback to SecurityContextHolder if currentUser is null (which can happen on permitAll endpoints)
                CustomUserDetails userDetails = currentUser;
                if (userDetails == null) {
                    org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                        userDetails = (CustomUserDetails) auth.getPrincipal();
                    }
                }
                boolean isOwnerRequesting = (userDetails != null && organizerId != null && userDetails.getId().equals(organizerId));
                if (!isOwnerRequesting) {
                    predicates.add(cb.notEqual(root.get("status"), EventStatus.DRAFT));
                    predicates.add(cb.notEqual(root.get("status"), EventStatus.CANCELLED));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return eventRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    private void validateEventDates(EventRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        if (request.getRegistrationDeadline().isAfter(request.getStartDate())) {
            throw new BadRequestException("Registration deadline cannot be after event start date");
        }
        
        if (request.getType() == EventType.HACKATHON && request.getParticipationType() == com.eventhub.model.ParticipationType.TEAM) {
            if (request.getTeamSizeMin() == null || request.getTeamSizeMin() < 1) {
                throw new BadRequestException("Min team size must be at least 1");
            }
            if (request.getTeamSizeMax() == null || request.getTeamSizeMax() < request.getTeamSizeMin()) {
                throw new BadRequestException("Max team size must be greater than or equal to min team size");
            }
        }
    }

    private void checkEventOwnershipOrAdmin(Event event, CustomUserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
    }

    private EventResponse mapToDto(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .type(event.getType())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getName())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .registrationDeadline(event.getRegistrationDeadline())
                .eligibility(event.getEligibility())
                .maxParticipants(event.getMaxParticipants())
                .teamSizeMin(event.getTeamSizeMin())
                .teamSizeMax(event.getTeamSizeMax())
                .registrationFee(event.getRegistrationFee())
                .status(event.getStatus())
                .rules(event.getRules())
                .prizes(event.getPrizes())
                .bannerUrl(event.getBannerUrl())
                .participationType(event.getParticipationType())
                .workshopMode(event.getWorkshopMode())
                .instructor(event.getInstructor())
                .capacity(event.getCapacity())
                .workshopLevel(event.getWorkshopLevel())
                .webinarPlatform(event.getWebinarPlatform())
                .webinarUrl(event.getWebinarUrl())
                .registrationType(event.getRegistrationType())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}

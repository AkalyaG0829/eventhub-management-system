package com.eventhub.service;

import com.eventhub.dto.RegistrationRequest;
import com.eventhub.dto.RegistrationResponse;
import com.eventhub.dto.TeamMemberRequest;
import com.eventhub.dto.TeamMemberResponse;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.*;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.TeamMemberRepository;
import com.eventhub.repository.TeamRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Transactional
    public RegistrationResponse registerForEvent(Long eventId, RegistrationRequest request, CustomUserDetails currentUser) {
        User participant = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (participant.getRole() != Role.PARTICIPANT) {
            throw new AccessDeniedException("Only participants can register for events");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Update participant's phone if missing and provided in request
        if ((participant.getPhone() == null || participant.getPhone().trim().isEmpty()) && request != null) {
            String newPhone = null;
            if (event.getParticipationType() == ParticipationType.TEAM && request.getMembers() != null && !request.getMembers().isEmpty()) {
                newPhone = request.getMembers().get(0).getPhone();
            } else if (request.getParticipantPhone() != null && !request.getParticipantPhone().trim().isEmpty()) {
                newPhone = request.getParticipantPhone();
            }
            
            if (newPhone != null && !newPhone.trim().isEmpty()) {
                if (!newPhone.matches("^\\\\+?[0-9\\\\-\\\\s]{10,15}$")) {
                    throw new BadRequestException("Invalid phone number format");
                }
                participant.setPhone(newPhone.trim());
                userRepository.save(participant);
            }
        }

        if (participant.getPhone() == null || participant.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Phone number is required for registration");
        }

        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("Cannot register for an event that is " + event.getStatus());
        }

        if (LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new ConflictException("Registration deadline has passed");
        }

        if (registrationRepository.existsByParticipantIdAndEventId(participant.getId(), event.getId())) {
            throw new ConflictException("You are already registered for this event");
        }

        if (event.getMaxParticipants() != null) {
            long currentRegistrations = registrationRepository.countByEventId(event.getId());
            if (currentRegistrations >= event.getMaxParticipants()) {
                throw new ConflictException("Event has reached its maximum capacity");
            }
        }

        // Validate and create Team if applicable
        Team savedTeam = null;
        if (event.getParticipationType() == ParticipationType.TEAM && request != null && request.getTeamSize() != null) {
            if (event.getTeamSizeMin() != null && request.getTeamSize() < event.getTeamSizeMin()) {
                throw new BadRequestException("Team size is below the minimum allowed (" + event.getTeamSizeMin() + ")");
            }
            if (event.getTeamSizeMax() != null && request.getTeamSize() > event.getTeamSizeMax()) {
                throw new BadRequestException("Team size is above the maximum allowed (" + event.getTeamSizeMax() + ")");
            }

            List<TeamMemberRequest> members = request.getMembers();
            if (members == null || members.size() != request.getTeamSize()) {
                throw new BadRequestException("The number of provided members must exactly match the selected team size");
            }

            if (teamRepository.existsByEventIdAndName(eventId, request.getTeamName())) {
                throw new ConflictException("Team name already exists for this event");
            }

            // Ensure Leader is Participant 1
            TeamMemberRequest leaderReq = members.get(0);
            if (!leaderReq.getEmail().equalsIgnoreCase(participant.getEmail())) {
                throw new BadRequestException("The logged-in participant must be Participant 1 (Team Leader)");
            }

            // Check for duplicate emails in the request
            Set<String> emailSet = new HashSet<>();
            for (TeamMemberRequest member : members) {
                if (!emailSet.add(member.getEmail().toLowerCase())) {
                    throw new BadRequestException("Duplicate email found in team members: " + member.getEmail());
                }
            }

            Team team = Team.builder()
                    .name(request.getTeamName())
                    .event(event)
                    .leader(participant)
                    .build();

            savedTeam = teamRepository.save(team);

            for (int i = 0; i < members.size(); i++) {
                TeamMemberRequest mReq = members.get(i);
                TeamMember member = TeamMember.builder()
                        .team(savedTeam)
                        .role(i == 0 ? TeamRole.LEADER : TeamRole.MEMBER)
                        .build();

                Optional<User> existingUser = userRepository.findByEmail(mReq.getEmail());
                if (existingUser.isPresent()) {
                    member.setUser(existingUser.get());
                } else {
                    member.setUnregisteredName(mReq.getName());
                    member.setUnregisteredEmail(mReq.getEmail());
                    member.setUnregisteredPhone(mReq.getPhone());
                }

                teamMemberRepository.save(member);
                savedTeam.getMembers().add(member);
            }
        }

        com.eventhub.model.PaymentStatus paymentStatus = com.eventhub.model.PaymentStatus.NOT_REQUIRED;
        if (event.getRegistrationType() == com.eventhub.model.RegistrationType.PAID) {
            paymentStatus = com.eventhub.model.PaymentStatus.PENDING;
        }

        Registration registration = Registration.builder()
                .participant(participant)
                .event(event)
                .status(RegistrationStatus.APPROVED) // default status
                .paymentStatus(paymentStatus)
                .build();

        Registration savedRegistration = registrationRepository.save(registration);
        return mapToDto(savedRegistration);
    }

    public List<RegistrationResponse> getMyRegistrations(CustomUserDetails currentUser) {
        List<Registration> registrations = registrationRepository.findByParticipantId(currentUser.getId());
        return registrations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<RegistrationResponse> getEventRegistrations(Long eventId, CustomUserDetails currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view registrations for this event");
        }

        List<Registration> registrations = registrationRepository.findByEventId(eventId);
        return registrations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private RegistrationResponse mapToDto(Registration registration) {
        RegistrationResponse response = RegistrationResponse.builder()
                .id(registration.getId())
                .participantId(registration.getParticipant().getId())
                .participantName(registration.getParticipant().getName())
                .participantEmail(registration.getParticipant().getEmail())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .status(registration.getStatus())
                .paymentStatus(registration.getPaymentStatus())
                .paymentAmount(registration.getEvent().getRegistrationFee())
                .registeredAt(registration.getRegisteredAt())
                .build();

        if (registration.getEvent().getParticipationType() == ParticipationType.TEAM) {
            Optional<Team> teamOpt = teamRepository.findByEventIdAndLeaderId(registration.getEvent().getId(), registration.getParticipant().getId());
            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                response.setTeamName(team.getName());
                response.setTeamSize(team.getMembers().size());
                
                List<TeamMemberResponse> memberResponses = team.getMembers().stream().map(m -> {
                    TeamMemberResponse mr = new TeamMemberResponse();
                    mr.setId(m.getId());
                    mr.setTeamId(team.getId());
                    mr.setRole(m.getRole());
                    mr.setJoinedAt(m.getJoinedAt());
                    
                    if (m.getUser() != null) {
                        mr.setUserId(m.getUser().getId());
                        mr.setUserName(m.getUser().getName());
                        mr.setMemberName(m.getUser().getName());
                        mr.setMemberEmail(m.getUser().getEmail());
                        mr.setMemberPhone(m.getUser().getPhone());
                    } else {
                        mr.setMemberName(m.getUnregisteredName());
                        mr.setMemberEmail(m.getUnregisteredEmail());
                        mr.setMemberPhone(m.getUnregisteredPhone());
                    }
                    return mr;
                }).collect(Collectors.toList());
                
                response.setTeamMembers(memberResponses);
            }
        }

        return response;
    }

    public RegistrationResponse simulatePayment(Long eventId, Long registrationId, boolean success, CustomUserDetails currentUser) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (!registration.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Registration does not belong to this event");
        }

        if (!registration.getParticipant().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only simulate payment for your own registration");
        }

        if (registration.getEvent().getRegistrationType() != com.eventhub.model.RegistrationType.PAID) {
            throw new BadRequestException("This event is not a PAID event");
        }

        if (registration.getPaymentStatus() != com.eventhub.model.PaymentStatus.PENDING) {
            throw new ConflictException("Payment is not pending for this registration");
        }

        if (success) {
            registration.setPaymentStatus(com.eventhub.model.PaymentStatus.PAID);
        } else {
            registration.setPaymentStatus(com.eventhub.model.PaymentStatus.FAILED);
        }

        Registration savedRegistration = registrationRepository.save(registration);
        return mapToDto(savedRegistration);
    }
}

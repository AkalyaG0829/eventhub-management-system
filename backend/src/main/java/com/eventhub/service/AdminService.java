package com.eventhub.service;

import com.eventhub.dto.admin.AdminStatsResponse;
import com.eventhub.dto.admin.AdminUserResponse;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.model.EventStatus;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.*;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    public AdminStatsResponse getSystemStats() {
        long totalUsers = userRepository.count();
        long totalParticipants = userRepository.countByRole(Role.PARTICIPANT);
        long totalOrganizers = userRepository.countByRole(Role.ORGANIZER);
        
        long activeUsers = userRepository.count((root, query, cb) -> cb.equal(root.get("enabled"), true));
        long inactiveUsers = userRepository.count((root, query, cb) -> cb.equal(root.get("enabled"), false));
        
        long totalEvents = eventRepository.count();
        long publishedEvents = eventRepository.countByStatus(EventStatus.PUBLISHED);
        long draftEvents = eventRepository.countByStatus(EventStatus.DRAFT);
        long cancelledEvents = eventRepository.countByStatus(EventStatus.CANCELLED);
        
        long totalRegistrations = registrationRepository.count();
        long totalTeams = teamRepository.count();
        long totalSubmissions = submissionRepository.count();
        
        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalParticipants(totalParticipants)
                .totalOrganizers(totalOrganizers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .draftEvents(draftEvents)
                .cancelledEvents(cancelledEvents)
                .totalRegistrations(totalRegistrations)
                .totalTeams(totalTeams)
                .totalSubmissions(totalSubmissions)
                .build();
    }

    public Page<AdminUserResponse> searchUsers(String search, Role role, Boolean enabled, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%");
                predicates.add(cb.or(nameMatch, emailMatch));
            }
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(this::mapToDto);
    }
    
    public AdminUserResponse updateUserStatus(Long userId, boolean enabled, CustomUserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (user.getId().equals(currentUser.getId())) {
            throw new ConflictException("You cannot change your own account status");
        }
        
        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }
    
    private AdminUserResponse mapToDto(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .college(user.getCollege())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .enabled(user.isEnabled())
                .build();
    }
}

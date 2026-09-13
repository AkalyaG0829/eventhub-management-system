package com.eventhub.controller;

import com.eventhub.model.Event;
import com.eventhub.model.EventStatus;
import com.eventhub.model.EventType;
import com.eventhub.model.Registration;
import com.eventhub.model.RegistrationStatus;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private User participant;
    private User organizer;
    private Event sampleEvent;
    private Event fullEvent;
    private Event draftEvent;
    private String participantToken;
    private String organizerToken;

    @BeforeEach
    void setUp() {
        organizer = userRepository.save(User.builder()
                .name("Organizer")
                .email("organizer_reg@test.com")
                .password("password")
                .role(Role.ORGANIZER)
                .build());

        participant = userRepository.save(User.builder()
                .name("Participant")
                .email("participant_reg@test.com")
                .password("password")
                .role(Role.PARTICIPANT)
                .phone("1234567890")
                .build());

        sampleEvent = eventRepository.save(Event.builder()
                .title("Sample Hackathon")
                .description("A great hackathon")
                .type(EventType.HACKATHON)
                .organizer(organizer)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(12))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .status(EventStatus.PUBLISHED)
                .maxParticipants(100)
                .build());

        fullEvent = eventRepository.save(Event.builder()
                .title("Full Event")
                .description("Full")
                .type(EventType.WORKSHOP)
                .organizer(organizer)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(12))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .status(EventStatus.PUBLISHED)
                .maxParticipants(1)
                .build());

        draftEvent = eventRepository.save(Event.builder()
                .title("Draft Event")
                .description("Draft")
                .type(EventType.WEBINAR)
                .organizer(organizer)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(12))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .status(EventStatus.DRAFT)
                .build());

        User dummyUser = userRepository.save(User.builder()
                .name("Dummy")
                .email("dummy@test.com")
                .password("password")
                .role(Role.PARTICIPANT)
                .phone("0987654321")
                .build());

        registrationRepository.save(Registration.builder()
                .participant(dummyUser)
                .event(fullEvent)
                .status(RegistrationStatus.APPROVED)
                .paymentStatus(com.eventhub.model.PaymentStatus.NOT_REQUIRED)
                .build());

        participantToken = generateToken(participant);
        organizerToken = generateToken(organizer);
    }

    private String generateToken(User user) {
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        return tokenProvider.generateToken(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testParticipantRegistration_Success() throws Exception {
        mockMvc.perform(post("/api/events/" + sampleEvent.getId() + "/register")
                .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isCreated());
    }

    @Test
    public void testDuplicateRegistration_Rejected() throws Exception {
        registrationRepository.save(Registration.builder()
                .participant(participant)
                .event(sampleEvent)
                .status(RegistrationStatus.APPROVED)
                .paymentStatus(com.eventhub.model.PaymentStatus.NOT_REQUIRED)
                .build());

        mockMvc.perform(post("/api/events/" + sampleEvent.getId() + "/register")
                .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegistrationForDraftEvent_Rejected() throws Exception {
        mockMvc.perform(post("/api/events/" + draftEvent.getId() + "/register")
                .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isConflict());
    }

    @Test
    public void testEventCapacityReached_Rejected() throws Exception {
        mockMvc.perform(post("/api/events/" + fullEvent.getId() + "/register")
                .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isConflict());
    }

    @Test
    public void testOrganizerAttemptingRegistration_Rejected() throws Exception {
        mockMvc.perform(post("/api/events/" + sampleEvent.getId() + "/register")
                .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isForbidden());
    }
}

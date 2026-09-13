package com.eventhub.controller;

import com.eventhub.dto.EventRequest;
import com.eventhub.model.Event;
import com.eventhub.model.EventStatus;
import com.eventhub.model.EventType;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User organizer;
    private User participant;
    private User admin;
    private Event sampleEvent;

    private String organizerToken;
    private String participantToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        organizer = userRepository.save(User.builder()
                .name("Organizer")
                .email("organizer@test.com")
                .password("password")
                .role(Role.ORGANIZER)
                .build());

        participant = userRepository.save(User.builder()
                .name("Participant")
                .email("participant@test.com")
                .password("password")
                .role(Role.PARTICIPANT)
                .build());

        admin = userRepository.save(User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password("password")
                .role(Role.ADMIN)
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
                .build());

        organizerToken = generateToken(organizer);
        participantToken = generateToken(participant);
        adminToken = generateToken(admin);
    }

    private String generateToken(User user) {
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        return tokenProvider.generateToken(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testGetPublishedEventsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testCreateEventAsOrganizer() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("New Event");
        request.setDescription("New Description");
        request.setType(EventType.WORKSHOP);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setRegistrationDeadline(LocalDateTime.now().plusHours(12));
        request.setRegistrationFee(BigDecimal.ZERO);

        mockMvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + organizerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Event"));
    }

    @Test
    public void testCreateEventAsParticipant_Forbidden() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("New Event");
        request.setDescription("New Description");
        request.setType(EventType.WORKSHOP);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setRegistrationDeadline(LocalDateTime.now().plusHours(12));
        request.setRegistrationFee(BigDecimal.ZERO);

        mockMvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + participantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateEventAsAdmin() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("Admin Event");
        request.setDescription("New Description");
        request.setType(EventType.WORKSHOP);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setRegistrationDeadline(LocalDateTime.now().plusHours(12));
        request.setRegistrationFee(BigDecimal.ZERO);

        mockMvc.perform(post("/api/events")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Admin Event"));
    }

    @Test
    public void testPublishEventByOrganizer() throws Exception {
        Event draftEvent = eventRepository.save(Event.builder()
                .title("Draft Event")
                .description("Draft")
                .type(EventType.WORKSHOP)
                .organizer(organizer)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(12))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .status(EventStatus.DRAFT)
                .build());

        mockMvc.perform(patch("/api/events/" + draftEvent.getId() + "/publish")
                .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    public void testPublishEventByParticipant_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/events/" + sampleEvent.getId() + "/publish")
                .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isForbidden());
    }
}

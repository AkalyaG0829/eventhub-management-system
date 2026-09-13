package com.eventhub.controller;

import com.eventhub.dto.TeamRequest;
import com.eventhub.model.*;
import com.eventhub.repository.*;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TeamControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User participant1;
    private User participant2;
    private User unregisteredParticipant;
    private Event sampleEvent;
    private String participant1Token;
    private String participant2Token;
    private String unregisteredToken;

    @BeforeEach
    void setUp() {
        User organizer = userRepository.save(User.builder()
                .name("Organizer")
                .email("organizer_team@test.com")
                .password("password")
                .role(Role.ORGANIZER)
                .build());

        participant1 = userRepository.save(User.builder()
                .name("Participant 1")
                .email("p1@test.com")
                .password("password")
                .role(Role.PARTICIPANT)
                .build());

        participant2 = userRepository.save(User.builder()
                .name("Participant 2")
                .email("p2@test.com")
                .password("password")
                .role(Role.PARTICIPANT)
                .build());

        unregisteredParticipant = userRepository.save(User.builder()
                .name("Unregistered")
                .email("unreg@test.com")
                .password("password")
                .role(Role.PARTICIPANT)
                .build());

        sampleEvent = eventRepository.save(Event.builder()
                .title("Sample Hackathon")
                .description("A great hackathon")
                .type(EventType.HACKATHON)
                .participationType(ParticipationType.TEAM)
                .organizer(organizer)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(12))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .status(EventStatus.PUBLISHED)
                .teamSizeMin(1)
                .teamSizeMax(3)
                .build());

        registrationRepository.save(Registration.builder()
                .participant(participant1)
                .event(sampleEvent)
                .status(RegistrationStatus.APPROVED)
                .paymentStatus(com.eventhub.model.PaymentStatus.NOT_REQUIRED)
                .build());

        registrationRepository.save(Registration.builder()
                .participant(participant2)
                .event(sampleEvent)
                .status(RegistrationStatus.APPROVED)
                .paymentStatus(com.eventhub.model.PaymentStatus.NOT_REQUIRED)
                .build());

        participant1Token = generateToken(participant1);
        participant2Token = generateToken(participant2);
        unregisteredToken = generateToken(unregisteredParticipant);
    }

    private String generateToken(User user) {
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        return tokenProvider.generateToken(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testCreateTeam_RegisteredParticipant_Success() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setName("Awesome Team");

        mockMvc.perform(post("/api/events/" + sampleEvent.getId() + "/teams")
                .header("Authorization", "Bearer " + participant1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Awesome Team"));
    }

    @Test
    public void testCreateTeam_UnregisteredParticipant_Forbidden() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setName("Unreg Team");

        mockMvc.perform(post("/api/events/" + sampleEvent.getId() + "/teams")
                .header("Authorization", "Bearer " + unregisteredToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testJoinTeam_Success() throws Exception {
        Team team = teamRepository.save(Team.builder()
                .name("Existing Team")
                .event(sampleEvent)
                .leader(participant1)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .team(team)
                .user(participant1)
                .role(TeamRole.LEADER)
                .build());

        mockMvc.perform(post("/api/teams/" + team.getId() + "/members")
                .header("Authorization", "Bearer " + participant2Token))
                .andExpect(status().isOk());
    }

    @Test
    public void testDuplicateTeamMembership_Rejected() throws Exception {
        Team team = teamRepository.save(Team.builder()
                .name("My Team")
                .event(sampleEvent)
                .leader(participant1)
                .build());

        teamMemberRepository.save(TeamMember.builder()
                .team(team)
                .user(participant1)
                .role(TeamRole.LEADER)
                .build());

        mockMvc.perform(post("/api/teams/" + team.getId() + "/members")
                .header("Authorization", "Bearer " + participant1Token))
                .andExpect(status().isConflict());
    }

    @Test
    public void testJoinTeamWhenAlreadyInAnotherTeam_Rejected() throws Exception {
        Team team1 = teamRepository.save(Team.builder().name("T1").event(sampleEvent).leader(participant1).build());
        teamMemberRepository.save(TeamMember.builder().team(team1).user(participant1).role(TeamRole.LEADER).build());

        Team team2 = teamRepository.save(Team.builder().name("T2").event(sampleEvent).leader(participant2).build());
        teamMemberRepository.save(TeamMember.builder().team(team2).user(participant2).role(TeamRole.LEADER).build());

        // Participant 1 tries to join team 2
        mockMvc.perform(post("/api/teams/" + team2.getId() + "/members")
                .header("Authorization", "Bearer " + participant1Token))
                .andExpect(status().isConflict());
    }
}

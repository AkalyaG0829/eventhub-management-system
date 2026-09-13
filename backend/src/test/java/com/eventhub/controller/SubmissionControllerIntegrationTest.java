package com.eventhub.controller;

import com.eventhub.dto.EvaluationRequest;
import com.eventhub.dto.SubmissionRequest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SubmissionControllerIntegrationTest {

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
    private SubmissionRepository submissionRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User organizer;
    private User admin;
    private User leader;
    private User member;
    private User unrelatedOrganizer;
    private Event sampleEvent;
    private Team sampleTeam;
    private Submission evaluatedSubmission;
    private Submission draftSubmission;

    private String organizerToken;
    private String leaderToken;
    private String memberToken;
    private String adminToken;
    private String unrelatedToken;

    @BeforeEach
    void setUp() {
        organizer = userRepository.save(User.builder().name("Org").email("org@test.com").password("pwd").role(Role.ORGANIZER).build());
        admin = userRepository.save(User.builder().name("Admin").email("adm@test.com").password("pwd").role(Role.ADMIN).build());
        leader = userRepository.save(User.builder().name("Leader").email("leader@test.com").password("pwd").role(Role.PARTICIPANT).build());
        member = userRepository.save(User.builder().name("Member").email("member@test.com").password("pwd").role(Role.PARTICIPANT).build());
        unrelatedOrganizer = userRepository.save(User.builder().name("Unrel Org").email("unrel@test.com").password("pwd").role(Role.ORGANIZER).build());

        sampleEvent = eventRepository.save(Event.builder()
                .title("Hackathon")
                .description("Desc")
                .type(EventType.HACKATHON)
                .organizer(organizer)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusDays(2))
                .registrationDeadline(LocalDateTime.now().minusDays(1))
                .status(EventStatus.ONGOING)
                .build());

        registrationRepository.save(Registration.builder().participant(leader).event(sampleEvent).status(RegistrationStatus.APPROVED).paymentStatus(com.eventhub.model.PaymentStatus.NOT_REQUIRED).build());
        registrationRepository.save(Registration.builder().participant(member).event(sampleEvent).status(RegistrationStatus.APPROVED).paymentStatus(com.eventhub.model.PaymentStatus.NOT_REQUIRED).build());

        sampleTeam = teamRepository.save(Team.builder().name("Team A").event(sampleEvent).leader(leader).build());
        teamMemberRepository.save(TeamMember.builder().team(sampleTeam).user(leader).role(TeamRole.LEADER).build());
        teamMemberRepository.save(TeamMember.builder().team(sampleTeam).user(member).role(TeamRole.MEMBER).build());

        evaluatedSubmission = submissionRepository.save(Submission.builder()
                .team(sampleTeam)
                .event(sampleEvent)
                .projectTitle("Evaluated Project")
                .description("Good project")
                .status(SubmissionStatus.EVALUATED)
                .build());

        evaluationRepository.save(Evaluation.builder()
                .submission(evaluatedSubmission)
                .judge(organizer)
                .innovationScore(new BigDecimal("9.0"))
                .technicalScore(new BigDecimal("9.0"))
                .uiUxScore(new BigDecimal("9.0"))
                .impactScore(new BigDecimal("9.0"))
                .presentationScore(new BigDecimal("9.0"))
                .totalScore(new BigDecimal("45.0"))
                .build());

        organizerToken = generateToken(organizer);
        leaderToken = generateToken(leader);
        memberToken = generateToken(member);
        adminToken = generateToken(admin);
        unrelatedToken = generateToken(unrelatedOrganizer);
    }

    private String generateToken(User user) {
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        return tokenProvider.generateToken(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testSubmitProject_Leader_Success() throws Exception {
        Team t2 = teamRepository.save(Team.builder().name("T2").event(sampleEvent).leader(leader).build());
        
        SubmissionRequest request = new SubmissionRequest();
        request.setProjectTitle("My Project");
        request.setDescription("It works");

        mockMvc.perform(post("/api/teams/" + t2.getId() + "/submissions")
                .header("Authorization", "Bearer " + leaderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectTitle").value("My Project"));
    }

    @Test
    public void testSubmitProject_Member_Forbidden() throws Exception {
        SubmissionRequest request = new SubmissionRequest();
        request.setProjectTitle("My Project");
        request.setDescription("It works");

        mockMvc.perform(post("/api/teams/" + sampleTeam.getId() + "/submissions")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testEvaluateSubmission_Organizer_Success() throws Exception {
        Submission submission = submissionRepository.save(Submission.builder()
                .team(sampleTeam)
                .event(sampleEvent)
                .projectTitle("To evaluate")
                .description("Eval me")
                .status(SubmissionStatus.SUBMITTED)
                .build());

        EvaluationRequest req = new EvaluationRequest();
        req.setInnovationScore(new BigDecimal("8.0"));
        req.setTechnicalScore(new BigDecimal("8.0"));
        req.setUiUxScore(new BigDecimal("8.0"));
        req.setImpactScore(new BigDecimal("8.0"));
        req.setPresentationScore(new BigDecimal("8.0"));

        mockMvc.perform(post("/api/submissions/" + submission.getId() + "/evaluations")
                .header("Authorization", "Bearer " + organizerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalScore").value("40.0"));
    }

    @Test
    public void testEvaluateSubmission_UnrelatedOrganizer_Forbidden() throws Exception {
        EvaluationRequest req = new EvaluationRequest();
        req.setInnovationScore(new BigDecimal("8.0"));
        req.setTechnicalScore(new BigDecimal("8.0"));
        req.setUiUxScore(new BigDecimal("8.0"));
        req.setImpactScore(new BigDecimal("8.0"));
        req.setPresentationScore(new BigDecimal("8.0"));

        mockMvc.perform(post("/api/submissions/" + evaluatedSubmission.getId() + "/evaluations")
                .header("Authorization", "Bearer " + unrelatedToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetLeaderboard_Unpublished_ForbiddenForParticipant() throws Exception {
        mockMvc.perform(get("/api/events/" + sampleEvent.getId() + "/leaderboard")
                .header("Authorization", "Bearer " + leaderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetLeaderboard_Unpublished_AllowedForOrganizer() throws Exception {
        mockMvc.perform(get("/api/events/" + sampleEvent.getId() + "/leaderboard")
                .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Team A"))
                .andExpect(jsonPath("$[0].totalScore").value("45.0"))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    public void testPublishResults_And_GetLeaderboardAsParticipant() throws Exception {
        mockMvc.perform(patch("/api/events/" + sampleEvent.getId() + "/results/publish")
                .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/" + sampleEvent.getId() + "/leaderboard")
                .header("Authorization", "Bearer " + leaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Team A"));
    }
}

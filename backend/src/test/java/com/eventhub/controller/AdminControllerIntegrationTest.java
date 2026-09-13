package com.eventhub.controller;

import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User targetUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("target_user@example.com").isEmpty()) {
            targetUser = User.builder()
                    .name("Target User")
                    .email("target_user@example.com")
                    .password("password123")
                    .role(Role.PARTICIPANT)
                    .enabled(true)
                    .build();
            targetUser = userRepository.save(targetUser);
        } else {
            targetUser = userRepository.findByEmail("target_user@example.com").get();
        }

        if (userRepository.findByEmail("admin_user@example.com").isEmpty()) {
            adminUser = User.builder()
                    .name("Admin User")
                    .email("admin_user@example.com")
                    .password("password123")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            adminUser = userRepository.save(adminUser);
        } else {
            adminUser = userRepository.findByEmail("admin_user@example.com").get();
        }

        if (userRepository.findByEmail("participant@example.com").isEmpty()) {
            User p = User.builder().name("p").email("participant@example.com").password("pwd").role(Role.PARTICIPANT).enabled(true).build();
            userRepository.save(p);
        }

        if (userRepository.findByEmail("organizer@example.com").isEmpty()) {
            User o = User.builder().name("o").email("organizer@example.com").password("pwd").role(Role.ORGANIZER).enabled(true).build();
            userRepository.save(o);
        }
    }

    @Test
    @org.springframework.security.test.context.support.WithUserDetails(value = "admin_user@example.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void testAdminCanDeactivateUser() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUser.getId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @org.springframework.security.test.context.support.WithUserDetails(value = "admin_user@example.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void testAdminCanActivateUser() throws Exception {
        targetUser.setEnabled(false);
        userRepository.save(targetUser);

        mockMvc.perform(patch("/api/admin/users/" + targetUser.getId() + "/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @org.springframework.security.test.context.support.WithUserDetails(value = "admin_user@example.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void testAdminCannotDeactivateSelf() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + adminUser.getId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @org.springframework.security.test.context.support.WithUserDetails(value = "participant@example.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void testParticipantReceives403() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUser.getId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.springframework.security.test.context.support.WithUserDetails(value = "organizer@example.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void testOrganizerReceives403() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + targetUser.getId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

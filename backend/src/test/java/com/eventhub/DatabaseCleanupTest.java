package com.eventhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class DatabaseCleanupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @Commit
    public void cleanup() {
        System.out.println("Altering table column type...");
        try {
            jdbcTemplate.execute("ALTER TABLE events CHANGE COLUMN access_type registration_type VARCHAR(255)");
        } catch(Exception e) {
            System.out.println("access_type already renamed or not found");
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE registrations ADD COLUMN payment_status VARCHAR(255) NOT NULL DEFAULT 'NOT_REQUIRED'");
        } catch(Exception e) {
            System.out.println("payment_status already exists");
        }
        
        try {
            jdbcTemplate.execute("UPDATE registrations SET payment_status = 'NOT_REQUIRED' WHERE payment_status IS NULL");
        } catch(Exception e) {
        }
        
        System.out.println("Starting cleanup...");
        
        // 1. Find all test event IDs
        // Test events are those created by test users, or having test titles
        String findTestEvents = "SELECT id FROM events WHERE title = 'EventHub Hackathon 2026' " +
                                "OR title LIKE 'Test %' " +
                                "OR title LIKE 'My Event%' " +
                                "OR title LIKE 'Sample Hackathon%' " +
                                "OR organizer_id IN (SELECT id FROM users WHERE email LIKE '%@test.com' OR email LIKE '%@example.com')";
                                
        java.util.List<Long> eventIds = jdbcTemplate.queryForList(findTestEvents, Long.class);
        System.out.println("Found " + eventIds.size() + " test events to delete.");
        
        if (!eventIds.isEmpty()) {
            String inClause = eventIds.toString().replace("[", "(").replace("]", ")");
            
            // Delete evaluations (depend on submissions)
            jdbcTemplate.update("DELETE FROM evaluations WHERE submission_id IN (SELECT id FROM submissions WHERE event_id IN " + inClause + ")");
            
            // Delete submissions
            jdbcTemplate.update("DELETE FROM submissions WHERE event_id IN " + inClause + "");
            
            // Delete team members (depend on teams)
            jdbcTemplate.update("DELETE FROM team_members WHERE team_id IN (SELECT id FROM teams WHERE event_id IN " + inClause + ")");
            
            // Delete teams
            jdbcTemplate.update("DELETE FROM teams WHERE event_id IN " + inClause + "");
            
            // Delete registrations
            jdbcTemplate.update("DELETE FROM registrations WHERE event_id IN " + inClause + "");
            
            // Delete announcements
            jdbcTemplate.update("DELETE FROM announcements WHERE event_id IN " + inClause + "");
            
            // Delete events
            jdbcTemplate.update("DELETE FROM events WHERE id IN " + inClause + "");
        }
        
        // 2. Find all test user IDs
        String findTestUsers = "SELECT id FROM users WHERE email LIKE '%@test.com' OR email LIKE '%@example.com' OR name IN ('Target User')";
        java.util.List<Long> userIds = jdbcTemplate.queryForList(findTestUsers, Long.class);
        System.out.println("Found " + userIds.size() + " test users to delete.");
        
        if (!userIds.isEmpty()) {
            String inClause = userIds.toString().replace("[", "(").replace("]", ")");
            
            // Some users might have registrations, teams, etc. for legitimate events.
            // Delete their evaluations
            jdbcTemplate.update("DELETE FROM evaluations WHERE judge_id IN " + inClause + "");
            // Delete their team_members
            jdbcTemplate.update("DELETE FROM team_members WHERE user_id IN " + inClause + "");
            // Delete their teams (and associated team_members)
            jdbcTemplate.update("DELETE FROM team_members WHERE team_id IN (SELECT id FROM teams WHERE leader_id IN " + inClause + ")");
            jdbcTemplate.update("DELETE FROM teams WHERE leader_id IN " + inClause + "");
            // Delete their submissions
            jdbcTemplate.update("DELETE FROM submissions WHERE team_id NOT IN (SELECT id FROM teams)"); // cleanup orphaned submissions just in case
            // Delete their registrations
            jdbcTemplate.update("DELETE FROM registrations WHERE participant_id IN " + inClause + "");
            
            // Delete users
            jdbcTemplate.update("DELETE FROM users WHERE id IN " + inClause + "");
        }
        
        System.out.println("Cleanup completed.");
    }
}

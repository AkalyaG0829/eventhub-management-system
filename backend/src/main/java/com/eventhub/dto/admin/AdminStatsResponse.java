package com.eventhub.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalParticipants;
    private long totalOrganizers;
    private long activeUsers;
    private long inactiveUsers;
    private long totalEvents;
    private long publishedEvents;
    private long draftEvents;
    private long cancelledEvents;
    private long totalRegistrations;
    private long totalTeams;
    private long totalSubmissions;
}

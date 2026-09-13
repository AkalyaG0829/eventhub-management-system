package com.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private Long eventId;
    private String eventTitle;
    private Long leaderId;
    private String leaderName;
    private List<TeamMemberResponse> members;
    private LocalDateTime createdAt;
}

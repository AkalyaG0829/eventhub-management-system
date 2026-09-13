package com.eventhub.dto;

import com.eventhub.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    private Long id;
    private Long teamId;
    private Long userId;
    private String userName;
    private String memberName; // Unified name
    private String memberEmail; // Unified email
    private String memberPhone; // Unified phone
    private TeamRole role;
    private LocalDateTime joinedAt;
}

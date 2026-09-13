package com.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponse {
    private Integer rank;
    private Long teamId;
    private String teamName;
    private Long submissionId;
    private String projectTitle;
    private BigDecimal totalScore;
}

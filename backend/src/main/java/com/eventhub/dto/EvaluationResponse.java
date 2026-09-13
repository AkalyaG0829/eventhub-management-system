package com.eventhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {
    private Long id;
    private Long submissionId;
    private Long judgeId;
    private String judgeName;
    private BigDecimal innovationScore;
    private BigDecimal technicalScore;
    private BigDecimal uiUxScore;
    private BigDecimal impactScore;
    private BigDecimal presentationScore;
    private BigDecimal totalScore;
    private String feedback;
    private LocalDateTime evaluatedAt;
}

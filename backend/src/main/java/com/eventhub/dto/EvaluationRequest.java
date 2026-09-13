package com.eventhub.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluationRequest {
    @NotNull(message = "Innovation score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private BigDecimal innovationScore;

    @NotNull(message = "Technical score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private BigDecimal technicalScore;

    @NotNull(message = "UI/UX score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private BigDecimal uiUxScore;

    @NotNull(message = "Impact score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private BigDecimal impactScore;

    @NotNull(message = "Presentation score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private BigDecimal presentationScore;

    private String feedback;
}

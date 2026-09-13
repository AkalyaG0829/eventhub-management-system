package com.eventhub.dto;

import com.eventhub.model.RegistrationStatus;
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
public class RegistrationResponse {
    private Long id;
    private Long participantId;
    private String participantName;
    private String participantEmail;
    private Long eventId;
    private String eventTitle;
    private RegistrationStatus status;
    private com.eventhub.model.PaymentStatus paymentStatus;
    private java.math.BigDecimal paymentAmount;
    private LocalDateTime registeredAt;
    
    // Optional Team Details
    private String teamName;
    private Integer teamSize;
    private List<TeamMemberResponse> teamMembers;
}

package com.eventhub.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String teamName;
    private Integer teamSize;
    private String participantPhone;
    
    @Valid
    private List<TeamMemberRequest> members;
}

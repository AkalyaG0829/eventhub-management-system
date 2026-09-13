package com.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamRequest {
    @NotBlank(message = "Team name is required")
    private String name;
}

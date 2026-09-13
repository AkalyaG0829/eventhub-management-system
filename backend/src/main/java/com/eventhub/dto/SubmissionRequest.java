package com.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotBlank(message = "Project title is required")
    private String projectTitle;

    @NotBlank(message = "Description is required")
    private String description;

    private String repositoryUrl;
    
    private String demoUrl;
}

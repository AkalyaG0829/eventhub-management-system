package com.eventhub.dto;

import com.eventhub.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long eventId;
    private String projectTitle;
    private String description;
    private String repositoryUrl;
    private String demoUrl;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
}

package com.eventhub.dto;

import com.eventhub.model.EventStatus;
import com.eventhub.model.EventType;
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
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private EventType type;
    private Long organizerId;
    private String organizerName;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationDeadline;
    private String eligibility;
    private Integer maxParticipants;
    private Integer teamSizeMin;
    private Integer teamSizeMax;
    private BigDecimal registrationFee;
    private com.eventhub.model.ParticipationType participationType;
    private com.eventhub.model.WorkshopMode workshopMode;
    private String instructor;
    private Integer capacity;
    private com.eventhub.model.WorkshopLevel workshopLevel;
    private com.eventhub.model.WebinarPlatform webinarPlatform;
    private String webinarUrl;
    private com.eventhub.model.RegistrationType registrationType;
    private EventStatus status;
    private String rules;
    private String prizes;
    private String bannerUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

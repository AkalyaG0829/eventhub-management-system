package com.eventhub.dto;

import com.eventhub.model.EventType;
import com.eventhub.model.ParticipationType;
import com.eventhub.model.WorkshopMode;
import com.eventhub.model.WorkshopLevel;
import com.eventhub.model.WebinarPlatform;
import com.eventhub.model.RegistrationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Event type is required")
    private EventType type;

    private String location;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @NotNull(message = "Registration deadline is required")
    @Future(message = "Registration deadline must be in the future")
    private LocalDateTime registrationDeadline;

    private String eligibility;

    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    @Min(value = 1, message = "Min team size must be at least 1")
    private Integer teamSizeMin;

    @Min(value = 1, message = "Max team size must be at least 1")
    private Integer teamSizeMax;

    @DecimalMin(value = "0.0", inclusive = true, message = "Registration fee must be positive or zero")
    private BigDecimal registrationFee;

    private ParticipationType participationType;

    private WorkshopMode workshopMode;

    private String instructor;

    private Integer capacity;

    private WorkshopLevel workshopLevel;

    private WebinarPlatform webinarPlatform;

    private String webinarUrl;

    private RegistrationType registrationType;

    private String rules;

    private String prizes;

    private String bannerUrl;
}

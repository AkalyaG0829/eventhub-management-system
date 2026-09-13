package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private String location;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    private Integer maxParticipants;

    private Integer teamSizeMin;

    private Integer teamSizeMax;

    private BigDecimal registrationFee;

    @Enumerated(EnumType.STRING)
    private ParticipationType participationType;

    @Enumerated(EnumType.STRING)
    private WorkshopMode workshopMode;

    private String instructor;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private WorkshopLevel workshopLevel;

    @Enumerated(EnumType.STRING)
    private WebinarPlatform webinarPlatform;

    private String webinarUrl;

    @Enumerated(EnumType.STRING)
    private RegistrationType registrationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(columnDefinition = "TEXT")
    private String prizes;

    private String bannerUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean resultsPublished = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

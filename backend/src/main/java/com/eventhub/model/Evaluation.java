package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    private User judge;

    @Column(precision = 5, scale = 2)
    private BigDecimal innovationScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal technicalScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal uiUxScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal impactScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal presentationScore;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal totalScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime evaluatedAt;
}

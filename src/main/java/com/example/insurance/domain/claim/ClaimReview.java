package com.example.insurance.domain.claim;

import com.example.insurance.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "claim_reviews")
public class ClaimReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", unique = true)
    private Claim claim;

    @Column(nullable = false)
    private String reviewerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewResult reviewResult;

    @Column(length = 1000)
    private String reviewComment;

    @Column(precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    public ClaimReview(
            Claim claim,
            String reviewerName,
            ReviewResult reviewResult,
            String reviewComment,
            BigDecimal approvedAmount
    ) {
        this.claim = claim;
        this.reviewerName = reviewerName;
        this.reviewResult = reviewResult;
        this.reviewComment = reviewComment;
        this.approvedAmount = approvedAmount;
        this.reviewedAt = LocalDateTime.now();
    }
}
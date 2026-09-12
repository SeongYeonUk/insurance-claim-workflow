package com.example.insurance.domain.claim;

import com.example.insurance.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "claim_histories")
public class ClaimHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Enumerated(EnumType.STRING)
    private ClaimStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus newStatus;

    @Column(nullable = false)
    private String changedBy;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public ClaimHistory(
            Claim claim,
            ClaimStatus previousStatus,
            ClaimStatus newStatus,
            String changedBy,
            String reason
    ) {
        this.claim = claim;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reason = reason;
        this.changedAt = LocalDateTime.now();
    }
}
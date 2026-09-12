package com.example.insurance.domain.claim;

import com.example.insurance.domain.common.BaseTimeEntity;
import com.example.insurance.domain.contract.InsuranceContract;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "claims",
        indexes = {
                @Index(
                        name = "idx_duplicate_check",
                        columnList = "contract_id, incident_date, claim_type"
                )
        }
)
public class Claim extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private InsuranceContract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, length = 1000)
    private String incidentDescription;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(nullable = false)
    private boolean duplicateSuspected;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    public Claim(
            String claimNumber,
            InsuranceContract contract,
            ClaimType claimType,
            LocalDate incidentDate,
            String incidentDescription,
            BigDecimal requestedAmount
    ) {
        this.claimNumber = claimNumber;
        this.contract = contract;
        this.claimType = claimType;
        this.incidentDate = incidentDate;
        this.incidentDescription = incidentDescription;
        this.requestedAmount = requestedAmount;

        this.status = ClaimStatus.RECEIVED;
        this.duplicateSuspected = false;
        this.submittedAt = LocalDateTime.now();
    }

    public void startDocumentReview() {

        validateStatus(ClaimStatus.RECEIVED);

        this.status = ClaimStatus.DOCUMENT_REVIEW;
    }
    public void requestAdditionalDocument() {

        validateStatus(ClaimStatus.DOCUMENT_REVIEW);

        this.status = ClaimStatus.ADDITIONAL_DOCUMENT_REQUIRED;
    }
    public void resumeDocumentReview() {

        validateStatus(ClaimStatus.ADDITIONAL_DOCUMENT_REQUIRED);

        this.status = ClaimStatus.DOCUMENT_REVIEW;
    }
    public void startReview() {

        validateStatus(ClaimStatus.DOCUMENT_REVIEW);

        this.status = ClaimStatus.UNDER_REVIEW;
    }
    private void validateStatus(ClaimStatus expectedStatus) {

        if (this.status != expectedStatus) {
            throw new IllegalStateException(
                    "현재 상태에서는 해당 작업을 수행할 수 없습니다. "
                            + "현재 상태: " + this.status
            );
        }
    }
    public void markDuplicateSuspected() {
        this.duplicateSuspected = true;
    }
    public void approve(BigDecimal approvedAmount) {

        validateStatus(ClaimStatus.UNDER_REVIEW);

        if (approvedAmount == null
                || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "승인금액은 0보다 커야 합니다."
            );
        }

        if (approvedAmount.compareTo(requestedAmount) > 0) {
            throw new IllegalArgumentException(
                    "승인금액은 청구금액을 초과할 수 없습니다."
            );
        }

        this.approvedAmount = approvedAmount;
        this.status = ClaimStatus.APPROVED;
    }
    public void reject() {

        validateStatus(ClaimStatus.UNDER_REVIEW);

        this.approvedAmount = null;
        this.status = ClaimStatus.REJECTED;
        this.completedAt = LocalDateTime.now();
    }
    public void markAsPaid() {

        validateStatus(ClaimStatus.APPROVED);

        this.status = ClaimStatus.PAID;
        this.completedAt = LocalDateTime.now();
    }
}
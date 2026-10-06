// ReinsuranceRecovery.java
package com.example.insurance.domain.reinsurance;

import com.example.insurance.domain.claim.ClaimPayment;
import com.example.insurance.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reinsurance_recoveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reinsurance_recovery_payment",
                        columnNames = "claim_payment_id"
                )
        }
)
public class ReinsuranceRecovery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "claim_payment_id",
            unique = true
    )
    private ClaimPayment claimPayment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reinsurance_contract_id")
    private ReinsuranceContract reinsuranceContract;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal recoveryAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    public ReinsuranceRecovery(
            ClaimPayment claimPayment,
            ReinsuranceContract reinsuranceContract
    ) {

        if (claimPayment == null) {
            throw new IllegalArgumentException(
                    "보험금 지급 내역이 필요합니다."
            );
        }

        if (reinsuranceContract == null) {
            throw new IllegalArgumentException(
                    "재보험 계약이 필요합니다."
            );
        }

        BigDecimal grossAmount =
                claimPayment.getPaymentAmount();

        BigDecimal recoveryAmount =
                grossAmount
                        .multiply(
                                reinsuranceContract
                                        .getCessionRate()
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal netAmount =
                grossAmount
                        .subtract(recoveryAmount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        this.claimPayment =
                claimPayment;
        this.reinsuranceContract =
                reinsuranceContract;
        this.grossAmount =
                grossAmount;
        this.recoveryAmount =
                recoveryAmount;
        this.netAmount =
                netAmount;
        this.calculatedAt =
                LocalDateTime.now();
    }
}
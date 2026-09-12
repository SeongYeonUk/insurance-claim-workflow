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
@Table(name = "claim_payments")
public class ClaimPayment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", unique = true)
    private Claim claim;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountNumberMasked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private LocalDateTime paidAt;

    public ClaimPayment(
            Claim claim,
            BigDecimal paymentAmount,
            String bankName,
            String accountNumberMasked
    ) {
        this.claim = claim;
        this.paymentAmount = paymentAmount;
        this.bankName = bankName;
        this.accountNumberMasked = accountNumberMasked;
        this.paymentStatus = PaymentStatus.READY;
    }
    public void complete() {

        if (paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException(
                    "지급 대기 상태에서만 완료 처리할 수 있습니다."
            );
        }

        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }
}
// ReinsuranceContract.java
package com.example.insurance.domain.reinsurance;

import com.example.insurance.domain.common.BaseTimeEntity;
import com.example.insurance.domain.contract.InsuranceContract;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reinsurance_contracts")
public class ReinsuranceContract extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reinsuranceContractNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insurance_contract_id", unique = true)
    private InsuranceContract insuranceContract;

    @Column(nullable = false)
    private String reinsurerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReinsuranceType reinsuranceType;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal cessionRate;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    public ReinsuranceContract(
            String reinsuranceContractNumber,
            InsuranceContract insuranceContract,
            String reinsurerName,
            ReinsuranceType reinsuranceType,
            BigDecimal cessionRate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateCessionRate(cessionRate);
        validatePeriod(startDate, endDate);

        this.reinsuranceContractNumber =
                reinsuranceContractNumber;
        this.insuranceContract =
                insuranceContract;
        this.reinsurerName =
                reinsurerName;
        this.reinsuranceType =
                reinsuranceType;
        this.cessionRate =
                cessionRate;
        this.startDate =
                startDate;
        this.endDate =
                endDate;
    }

    private void validateCessionRate(
            BigDecimal cessionRate
    ) {

        if (cessionRate == null
                || cessionRate.compareTo(
                BigDecimal.ZERO
        ) <= 0
                || cessionRate.compareTo(
                BigDecimal.ONE
        ) > 0) {

            throw new IllegalArgumentException(
                    "출재율은 0보다 크고 1 이하이어야 합니다."
            );
        }
    }

    private void validatePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null
                || endDate == null
                || startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "유효한 재보험 계약기간이 아닙니다."
            );
        }
    }

    public void validateApplicable(
            LocalDate incidentDate
    ) {

        if (reinsuranceType
                != ReinsuranceType.QUOTA_SHARE) {

            throw new IllegalStateException(
                    "현재는 Quota Share 재보험만 지원합니다."
            );
        }

        if (incidentDate.isBefore(startDate)
                || incidentDate.isAfter(endDate)) {

            throw new IllegalStateException(
                    "재보험 계약기간에 포함되지 않는 사고입니다."
            );
        }
    }
}
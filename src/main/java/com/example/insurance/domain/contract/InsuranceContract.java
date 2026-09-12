package com.example.insurance.domain.contract;

import com.example.insurance.domain.common.BaseTimeEntity;
import com.example.insurance.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "insurance_contracts")
public class InsuranceContract extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String contractNumber;

    @Column(nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus contractStatus;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String policyHolderName;

    @Column(nullable = false)
    private String insuredName;

    @Column(nullable = false)
    private String beneficiaryName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public InsuranceContract(
            String contractNumber,
            String productName,
            LocalDate startDate,
            LocalDate endDate,
            String policyHolderName,
            String insuredName,
            String beneficiaryName,
            Customer customer
    ) {
        this.contractNumber = contractNumber;
        this.productName = productName;
        this.contractStatus = ContractStatus.ACTIVE;
        this.startDate = startDate;
        this.endDate = endDate;
        this.policyHolderName = policyHolderName;
        this.insuredName = insuredName;
        this.beneficiaryName = beneficiaryName;
        this.customer = customer;
    }

    public void validateClaimable(LocalDate incidentDate) {

        if (contractStatus != ContractStatus.ACTIVE) {
            throw new IllegalStateException(
                    "유효한 보험계약이 아닙니다."
            );
        }

        if (incidentDate.isBefore(startDate)
                || incidentDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "보험사고 발생일이 보험기간에 포함되지 않습니다."
            );
        }
    }
}


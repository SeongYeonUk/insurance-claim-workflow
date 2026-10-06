package com.example.insurance.service;

import com.example.insurance.domain.claim.ClaimPayment;
import com.example.insurance.domain.claim.PaymentStatus;
import com.example.insurance.domain.contract.InsuranceContract;
import com.example.insurance.domain.reinsurance.ReinsuranceContract;
import com.example.insurance.domain.reinsurance.ReinsuranceRecovery;
import com.example.insurance.domain.reinsurance.ReinsuranceType;
import com.example.insurance.repository.ClaimPaymentRepository;
import com.example.insurance.repository.InsuranceContractRepository;
import com.example.insurance.repository.ReinsuranceContractRepository;
import com.example.insurance.repository.ReinsuranceRecoveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.insurance.dto.ReinsuranceRecoveryResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class ReinsuranceService {

    private final InsuranceContractRepository insuranceContractRepository;
    private final ClaimPaymentRepository claimPaymentRepository;
    private final ReinsuranceContractRepository reinsuranceContractRepository;
    private final ReinsuranceRecoveryRepository reinsuranceRecoveryRepository;

    public Long createReinsuranceContract(
            Long insuranceContractId,
            String reinsuranceContractNumber,
            String reinsurerName,
            BigDecimal cessionRate,
            LocalDate startDate,
            LocalDate endDate
    ) {

        InsuranceContract insuranceContract =
                insuranceContractRepository
                        .findById(insuranceContractId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "원보험 계약을 찾을 수 없습니다."
                                )
                        );

        if (reinsuranceContractRepository
                .existsByInsuranceContractId(
                        insuranceContractId
                )) {

            throw new IllegalStateException(
                    "이미 재보험 계약이 등록된 원보험 계약입니다."
            );
        }

        ReinsuranceContract reinsuranceContract =
                new ReinsuranceContract(
                        reinsuranceContractNumber,
                        insuranceContract,
                        reinsurerName,
                        ReinsuranceType.QUOTA_SHARE,
                        cessionRate,
                        startDate,
                        endDate
                );

        return reinsuranceContractRepository
                .save(reinsuranceContract)
                .getId();
    }

    public Long createRecovery(
            Long claimId
    ) {

        ClaimPayment claimPayment =
                claimPaymentRepository
                        .findByClaimId(claimId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "보험금 지급 내역을 찾을 수 없습니다."
                                )
                        );

        if (claimPayment.getPaymentStatus()
                != PaymentStatus.COMPLETED) {

            throw new IllegalStateException(
                    "보험금 지급이 완료된 청구만 재보험 회수 처리할 수 있습니다."
            );
        }

        if (reinsuranceRecoveryRepository
                .existsByClaimPaymentId(
                        claimPayment.getId()
                )) {

            throw new IllegalStateException(
                    "이미 재보험 회수 처리가 완료된 지급 건입니다."
            );
        }

        InsuranceContract insuranceContract =
                claimPayment
                        .getClaim()
                        .getContract();

        ReinsuranceContract reinsuranceContract =
                reinsuranceContractRepository
                        .findByInsuranceContractId(
                                insuranceContract.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "적용 가능한 재보험 계약을 찾을 수 없습니다."
                                )
                        );

        reinsuranceContract.validateApplicable(
                claimPayment
                        .getClaim()
                        .getIncidentDate()
        );

        ReinsuranceRecovery recovery =
                new ReinsuranceRecovery(
                        claimPayment,
                        reinsuranceContract
                );

        return reinsuranceRecoveryRepository
                .save(recovery)
                .getId();
    }
    @Transactional(readOnly = true)
    public ReinsuranceRecoveryResponse getRecovery(
            Long claimId
    ) {

        ClaimPayment claimPayment =
                claimPaymentRepository
                        .findByClaimId(claimId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "보험금 지급 내역을 찾을 수 없습니다."
                                )
                        );

        ReinsuranceRecovery recovery =
                reinsuranceRecoveryRepository
                        .findByClaimPaymentId(
                                claimPayment.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "재보험 회수 내역을 찾을 수 없습니다."
                                )
                        );

        return new ReinsuranceRecoveryResponse(
                recovery.getId(),
                claimId,
                recovery
                        .getReinsuranceContract()
                        .getReinsuranceContractNumber(),
                recovery
                        .getReinsuranceContract()
                        .getReinsurerName(),
                recovery
                        .getReinsuranceContract()
                        .getCessionRate(),
                recovery.getGrossAmount(),
                recovery.getRecoveryAmount(),
                recovery.getNetAmount(),
                recovery.getCalculatedAt()
        );
    }
}
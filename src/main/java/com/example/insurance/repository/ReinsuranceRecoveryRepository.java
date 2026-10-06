package com.example.insurance.repository;

import com.example.insurance.domain.reinsurance.ReinsuranceRecovery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReinsuranceRecoveryRepository
        extends JpaRepository<ReinsuranceRecovery, Long> {

    boolean existsByClaimPaymentId(
            Long claimPaymentId
    );

    Optional<ReinsuranceRecovery>
    findByClaimPaymentId(
            Long claimPaymentId
    );
}
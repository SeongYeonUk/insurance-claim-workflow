package com.example.insurance.repository;

import com.example.insurance.domain.claim.ClaimPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClaimPaymentRepository
        extends JpaRepository<ClaimPayment, Long> {

    boolean existsByClaimId(Long claimId);

    Optional<ClaimPayment> findByClaimId(Long claimId);
}
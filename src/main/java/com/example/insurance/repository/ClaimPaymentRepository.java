package com.example.insurance.repository;

import com.example.insurance.domain.claim.ClaimPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimPaymentRepository
        extends JpaRepository<ClaimPayment, Long> {

    boolean existsByClaimId(Long claimId);
}
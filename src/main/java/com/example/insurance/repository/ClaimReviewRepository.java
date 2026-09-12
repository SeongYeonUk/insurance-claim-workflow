package com.example.insurance.repository;

import com.example.insurance.domain.claim.ClaimReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimReviewRepository
        extends JpaRepository<ClaimReview, Long> {

    boolean existsByClaimId(Long claimId);
}
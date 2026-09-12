package com.example.insurance.repository;

import com.example.insurance.domain.claim.ClaimHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimHistoryRepository
        extends JpaRepository<ClaimHistory, Long> {

    List<ClaimHistory> findByClaimIdOrderByChangedAtAsc(Long claimId);
}
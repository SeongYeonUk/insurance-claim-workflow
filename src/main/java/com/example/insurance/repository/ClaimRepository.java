package com.example.insurance.repository;

import com.example.insurance.domain.claim.Claim;
import com.example.insurance.domain.claim.ClaimType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

import com.example.insurance.domain.claim.ClaimStatus;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    boolean existsByContractIdAndIncidentDateAndClaimType(
            Long contractId,
            LocalDate incidentDate,
            ClaimType claimType
    );

    long countByStatus(ClaimStatus status);

    long countByDuplicateSuspectedTrue();
}
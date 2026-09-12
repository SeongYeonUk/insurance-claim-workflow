package com.example.insurance.dto;

import com.example.insurance.domain.claim.ClaimStatus;
import com.example.insurance.domain.claim.ClaimType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponse(

        Long id,
        String claimNumber,
        String contractNumber,
        ClaimType claimType,
        LocalDate incidentDate,
        String incidentDescription,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        ClaimStatus status,
        boolean duplicateSuspected,
        LocalDateTime submittedAt,
        LocalDateTime completedAt
) {
}
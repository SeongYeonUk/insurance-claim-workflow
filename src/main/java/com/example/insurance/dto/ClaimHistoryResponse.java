package com.example.insurance.dto;

import com.example.insurance.domain.claim.ClaimStatus;

import java.time.LocalDateTime;

public record ClaimHistoryResponse(

        ClaimStatus previousStatus,
        ClaimStatus newStatus,
        String changedBy,
        String reason,
        LocalDateTime changedAt
) {
}
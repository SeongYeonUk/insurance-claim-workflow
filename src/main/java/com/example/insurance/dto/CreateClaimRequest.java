package com.example.insurance.dto;

import com.example.insurance.domain.claim.ClaimType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(

        @NotNull
        Long contractId,

        @NotNull
        ClaimType claimType,

        @NotNull
        LocalDate incidentDate,

        @NotBlank
        String incidentDescription,

        @NotNull
        @Positive
        BigDecimal requestedAmount
) {
}
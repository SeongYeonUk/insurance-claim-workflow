package com.example.insurance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ApproveClaimRequest(

        @NotBlank
        String reviewerName,

        @NotNull
        @Positive
        BigDecimal approvedAmount,

        String comment
) {
}
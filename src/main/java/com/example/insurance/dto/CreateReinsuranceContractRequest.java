package com.example.insurance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReinsuranceContractRequest(

        @NotNull
        Long insuranceContractId,

        @NotBlank
        String reinsuranceContractNumber,

        @NotBlank
        String reinsurerName,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax(value = "1.0")
        BigDecimal cessionRate,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}
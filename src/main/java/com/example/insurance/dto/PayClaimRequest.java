package com.example.insurance.dto;

import jakarta.validation.constraints.NotBlank;

public record PayClaimRequest(

        @NotBlank
        String bankName,

        @NotBlank
        String accountNumberMasked
) {
}
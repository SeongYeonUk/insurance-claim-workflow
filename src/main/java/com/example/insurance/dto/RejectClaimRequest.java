package com.example.insurance.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectClaimRequest(

        @NotBlank
        String reviewerName,

        @NotBlank
        String reason
) {
}
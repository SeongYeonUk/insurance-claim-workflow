package com.example.insurance.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewerRequest(

        @NotBlank
        String reviewerName
) {
}
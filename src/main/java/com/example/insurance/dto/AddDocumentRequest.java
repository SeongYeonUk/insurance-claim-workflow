package com.example.insurance.dto;

import com.example.insurance.domain.claim.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddDocumentRequest(

        @NotNull
        DocumentType documentType,

        @NotBlank
        String fileName
) {
}
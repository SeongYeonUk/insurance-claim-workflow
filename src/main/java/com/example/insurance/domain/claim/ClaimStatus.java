package com.example.insurance.domain.claim;

public enum ClaimStatus {
    RECEIVED,
    DOCUMENT_REVIEW,
    ADDITIONAL_DOCUMENT_REQUIRED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    PAID
}
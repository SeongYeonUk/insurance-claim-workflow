package com.example.insurance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReinsuranceRecoveryResponse(

        Long recoveryId,

        Long claimId,

        String reinsuranceContractNumber,

        String reinsurerName,

        BigDecimal cessionRate,

        BigDecimal grossAmount,

        BigDecimal recoveryAmount,

        BigDecimal netAmount,

        LocalDateTime calculatedAt
) {
}
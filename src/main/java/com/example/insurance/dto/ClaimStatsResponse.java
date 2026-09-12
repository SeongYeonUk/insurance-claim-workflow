package com.example.insurance.dto;

import java.util.Map;

public record ClaimStatsResponse(

        long totalClaims,
        long duplicateSuspectedClaims,
        Map<String, Long> statusCounts
) {
}
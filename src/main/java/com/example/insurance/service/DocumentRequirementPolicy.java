package com.example.insurance.service;

import com.example.insurance.domain.claim.ClaimType;
import com.example.insurance.domain.claim.DocumentType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class DocumentRequirementPolicy {

    private static final Map<ClaimType, Set<DocumentType>>
            REQUIRED_DOCUMENTS = Map.of(

            ClaimType.HOSPITALIZATION,
            Set.of(
                    DocumentType.DIAGNOSIS_CERTIFICATE,
                    DocumentType.HOSPITALIZATION_CERTIFICATE,
                    DocumentType.MEDICAL_RECEIPT
            ),

            ClaimType.SURGERY,
            Set.of(
                    DocumentType.SURGERY_CERTIFICATE,
                    DocumentType.MEDICAL_RECEIPT
            ),

            ClaimType.DIAGNOSIS,
            Set.of(
                    DocumentType.DIAGNOSIS_CERTIFICATE
            ),

            ClaimType.ACCIDENT,
            Set.of(
                    DocumentType.ACCIDENT_REPORT,
                    DocumentType.MEDICAL_RECEIPT
            ),

            ClaimType.DEATH,
            Set.of(
                    DocumentType.DEATH_CERTIFICATE
            ),

            ClaimType.OTHER,
            Set.of()
    );

    public Set<DocumentType> getRequiredDocuments(
            ClaimType claimType
    ) {
        return REQUIRED_DOCUMENTS.getOrDefault(
                claimType,
                Set.of()
        );
    }
}
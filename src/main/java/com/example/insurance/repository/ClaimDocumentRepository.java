package com.example.insurance.repository;

import com.example.insurance.domain.claim.ClaimDocument;
import com.example.insurance.domain.claim.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimDocumentRepository
        extends JpaRepository<ClaimDocument, Long> {

    List<ClaimDocument> findByClaimId(Long claimId);

    boolean existsByClaimIdAndDocumentType(
            Long claimId,
            DocumentType documentType
    );
}
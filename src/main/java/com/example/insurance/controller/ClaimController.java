package com.example.insurance.controller;

import com.example.insurance.domain.claim.DocumentType;
import com.example.insurance.dto.*;
import com.example.insurance.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import com.example.insurance.dto.ReviewerRequest;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<CreateClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request
    ) {

        Long claimId = claimService.createClaim(
                request.contractId(),
                request.claimType(),
                request.incidentDate(),
                request.incidentDescription(),
                request.requestedAmount()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new CreateClaimResponse(
                                claimId
                        )
                );
    }

    @GetMapping("/{claimId}")
    public ClaimResponse getClaim(
            @PathVariable Long claimId
    ) {

        return claimService.getClaim(claimId);
    }

    @GetMapping("/{claimId}/histories")
    public List<ClaimHistoryResponse> getHistories(
            @PathVariable Long claimId
    ) {

        return claimService
                .getClaimHistories(claimId);
    }

    @PostMapping("/{claimId}/document-review/start")
    public ResponseEntity<Void> startDocumentReview(
            @PathVariable Long claimId,
            @Valid @RequestBody ReviewerRequest request
    ) {

        claimService.startDocumentReview(
                claimId,
                request.reviewerName()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{claimId}/documents")
    public ResponseEntity<Long> addDocument(
            @PathVariable Long claimId,
            @Valid @RequestBody AddDocumentRequest request
    ) {

        Long documentId =
                claimService.addDocument(
                        claimId,
                        request.documentType(),
                        request.fileName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentId);
    }

    @GetMapping("/{claimId}/missing-documents")
    public Set<DocumentType> getMissingDocuments(
            @PathVariable Long claimId
    ) {

        return claimService
                .findMissingDocuments(claimId);
    }

    @PostMapping("/{claimId}/documents/validate")
    public ResponseEntity<Void> validateDocuments(
            @PathVariable Long claimId,
            @Valid @RequestBody ReviewerRequest request
    ) {

        claimService.validateDocuments(
                claimId,
                request.reviewerName()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{claimId}/document-review/resume")
    public ResponseEntity<Void> resumeDocumentReview(
            @PathVariable Long claimId,
            @Valid @RequestBody ReviewerRequest request
    ) {

        claimService.resumeDocumentReview(
                claimId,
                request.reviewerName()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{claimId}/review/approve")
    public ResponseEntity<Void> approveClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ApproveClaimRequest request
    ) {

        claimService.approveClaim(
                claimId,
                request.reviewerName(),
                request.approvedAmount(),
                request.comment()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{claimId}/review/reject")
    public ResponseEntity<Void> rejectClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody RejectClaimRequest request
    ) {

        claimService.rejectClaim(
                claimId,
                request.reviewerName(),
                request.reason()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{claimId}/payment")
    public ResponseEntity<Void> payClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody PayClaimRequest request
    ) {

        claimService.payClaim(
                claimId,
                request.bankName(),
                request.accountNumberMasked()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ClaimStatsResponse getStats() {

        return claimService.getClaimStats();
    }
}
package com.example.insurance.service;

import com.example.insurance.domain.claim.*;
import com.example.insurance.domain.contract.InsuranceContract;
import com.example.insurance.repository.ClaimHistoryRepository;
import com.example.insurance.repository.ClaimRepository;
import com.example.insurance.repository.InsuranceContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.example.insurance.repository.ClaimDocumentRepository;
import com.example.insurance.repository.ClaimPaymentRepository;
import com.example.insurance.repository.ClaimReviewRepository;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.insurance.dto.ClaimHistoryResponse;
import com.example.insurance.dto.ClaimResponse;

import java.util.List;

import com.example.insurance.dto.ClaimStatsResponse;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimService {

    private final InsuranceContractRepository contractRepository;
    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository historyRepository;

    private final ClaimDocumentRepository documentRepository;
    private final ClaimReviewRepository reviewRepository;
    private final ClaimPaymentRepository paymentRepository;
    private final DocumentRequirementPolicy documentRequirementPolicy;

    public Long createClaim(
            Long contractId,
            ClaimType claimType,
            LocalDate incidentDate,
            String incidentDescription,
            BigDecimal requestedAmount
    ) {

        InsuranceContract contract =
                contractRepository.findById(contractId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "보험계약을 찾을 수 없습니다."
                                )
                        );

        contract.validateClaimable(incidentDate);

        validateRequestedAmount(requestedAmount);

        String claimNumber = generateClaimNumber();

        boolean duplicateSuspected =
                claimRepository
                        .existsByContractIdAndIncidentDateAndClaimType(
                                contractId,
                                incidentDate,
                                claimType
                        );

        Claim claim = new Claim(
                claimNumber,
                contract,
                claimType,
                incidentDate,
                incidentDescription,
                requestedAmount
        );

        if (duplicateSuspected) {
            claim.markDuplicateSuspected();
        }

        Claim savedClaim = claimRepository.save(claim);

        ClaimHistory history = new ClaimHistory(
                savedClaim,
                null,
                ClaimStatus.RECEIVED,
                "CUSTOMER",
                "보험금 청구 접수"
        );

        historyRepository.save(history);

        return savedClaim.getId();
    }

    private void validateRequestedAmount(BigDecimal requestedAmount) {

        if (requestedAmount == null
                || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "청구금액은 0보다 커야 합니다."
            );
        }
    }

    private String generateClaimNumber() {

        return "CLM-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private void saveHistory(
            Claim claim,
            ClaimStatus previousStatus,
            ClaimStatus newStatus,
            String changedBy,
            String reason
    ) {

        ClaimHistory history = new ClaimHistory(
                claim,
                previousStatus,
                newStatus,
                changedBy,
                reason
        );

        historyRepository.save(history);
    }

    public void startDocumentReview(
            Long claimId,
            String reviewerName
    ) {

        Claim claim = findClaim(claimId);

        ClaimStatus previousStatus = claim.getStatus();

        claim.startDocumentReview();

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                reviewerName,
                "서류 검토 시작"
        );
    }

    public void requestAdditionalDocument(
            Long claimId,
            String reviewerName,
            String reason
    ) {

        Claim claim = findClaim(claimId);

        ClaimStatus previousStatus = claim.getStatus();

        claim.requestAdditionalDocument();

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                reviewerName,
                reason
        );
    }

    public void resumeDocumentReview(
            Long claimId,
            String reviewerName
    ) {

        Claim claim = findClaim(claimId);

        ClaimStatus previousStatus = claim.getStatus();

        claim.resumeDocumentReview();

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                reviewerName,
                "추가서류 제출 완료 후 재검토"
        );
    }

    public void startReview(
            Long claimId,
            String reviewerName
    ) {

        Claim claim = findClaim(claimId);

        ClaimStatus previousStatus = claim.getStatus();

        claim.startReview();

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                reviewerName,
                "지급심사 시작"
        );
    }

    private Claim findClaim(Long claimId) {

        return claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "보험금 청구건을 찾을 수 없습니다."
                        )
                );
    }
    public Long addDocument(
            Long claimId,
            DocumentType documentType,
            String fileName
    ) {

        Claim claim = findClaim(claimId);

        if (documentRepository.existsByClaimIdAndDocumentType(
                claimId,
                documentType
        )) {
            throw new IllegalStateException(
                    "이미 제출된 서류입니다."
            );
        }

        ClaimDocument document =
                new ClaimDocument(
                        claim,
                        documentType,
                        fileName
                );

        return documentRepository.save(document).getId();
    }
    public Set<DocumentType> findMissingDocuments(
            Long claimId
    ) {

        Claim claim = findClaim(claimId);

        Set<DocumentType> required =
                documentRequirementPolicy
                        .getRequiredDocuments(
                                claim.getClaimType()
                        );

        Set<DocumentType> submitted =
                documentRepository
                        .findByClaimId(claimId)
                        .stream()
                        .map(ClaimDocument::getDocumentType)
                        .collect(Collectors.toSet());

        return required.stream()
                .filter(type -> !submitted.contains(type))
                .collect(Collectors.toSet());
    }
    public void validateDocuments(
            Long claimId,
            String reviewerName
    ) {

        Claim claim = findClaim(claimId);

        if (claim.getStatus()
                != ClaimStatus.DOCUMENT_REVIEW) {

            throw new IllegalStateException(
                    "서류 검토 상태에서만 검증할 수 있습니다."
            );
        }

        Set<DocumentType> missing =
                findMissingDocuments(claimId);

        ClaimStatus previousStatus =
                claim.getStatus();

        if (missing.isEmpty()) {

            claim.startReview();

            saveHistory(
                    claim,
                    previousStatus,
                    claim.getStatus(),
                    reviewerName,
                    "필수서류 검증 완료"
            );

        } else {

            claim.requestAdditionalDocument();

            saveHistory(
                    claim,
                    previousStatus,
                    claim.getStatus(),
                    reviewerName,
                    "필수서류 누락: " + missing
            );
        }
    }
    public void approveClaim(
            Long claimId,
            String reviewerName,
            BigDecimal approvedAmount,
            String comment
    ) {

        Claim claim = findClaim(claimId);

        if (reviewRepository.existsByClaimId(claimId)) {
            throw new IllegalStateException(
                    "이미 심사가 완료된 청구입니다."
            );
        }

        ClaimStatus previousStatus =
                claim.getStatus();

        claim.approve(approvedAmount);

        ClaimReview review =
                new ClaimReview(
                        claim,
                        reviewerName,
                        ReviewResult.APPROVED,
                        comment,
                        approvedAmount
                );

        reviewRepository.save(review);

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                reviewerName,
                "보험금 지급 승인"
        );
    }
    public void rejectClaim(
            Long claimId,
            String reviewerName,
            String reason
    ) {

        Claim claim = findClaim(claimId);

        if (reviewRepository.existsByClaimId(claimId)) {
            throw new IllegalStateException(
                    "이미 심사가 완료된 청구입니다."
            );
        }

        ClaimStatus previousStatus =
                claim.getStatus();

        claim.reject();

        ClaimReview review =
                new ClaimReview(
                        claim,
                        reviewerName,
                        ReviewResult.REJECTED,
                        reason,
                        null
                );

        reviewRepository.save(review);

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                reviewerName,
                reason
        );
    }
    public void payClaim(
            Long claimId,
            String bankName,
            String accountNumberMasked
    ) {

        Claim claim = findClaim(claimId);

        if (claim.getStatus()
                != ClaimStatus.APPROVED) {

            throw new IllegalStateException(
                    "지급 승인된 청구만 보험금을 지급할 수 있습니다."
            );
        }

        if (paymentRepository.existsByClaimId(claimId)) {

            throw new IllegalStateException(
                    "이미 지급 처리된 청구입니다."
            );
        }

        ClaimStatus previousStatus =
                claim.getStatus();

        ClaimPayment payment =
                new ClaimPayment(
                        claim,
                        claim.getApprovedAmount(),
                        bankName,
                        accountNumberMasked
                );

        payment.complete();

        paymentRepository.save(payment);

        claim.markAsPaid();

        saveHistory(
                claim,
                previousStatus,
                claim.getStatus(),
                "SYSTEM",
                "보험금 지급 완료"
        );
    }
    @Transactional(readOnly = true)
    public ClaimResponse getClaim(Long claimId) {

        Claim claim = findClaim(claimId);

        return new ClaimResponse(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getContract().getContractNumber(),
                claim.getClaimType(),
                claim.getIncidentDate(),
                claim.getIncidentDescription(),
                claim.getRequestedAmount(),
                claim.getApprovedAmount(),
                claim.getStatus(),
                claim.isDuplicateSuspected(),
                claim.getSubmittedAt(),
                claim.getCompletedAt()
        );
    }
    @Transactional(readOnly = true)
    public List<ClaimHistoryResponse> getClaimHistories(
            Long claimId
    ) {

        findClaim(claimId);

        return historyRepository
                .findByClaimIdOrderByChangedAtAsc(claimId)
                .stream()
                .map(history ->
                        new ClaimHistoryResponse(
                                history.getPreviousStatus(),
                                history.getNewStatus(),
                                history.getChangedBy(),
                                history.getReason(),
                                history.getChangedAt()
                        )
                )
                .toList();
    }
    @Transactional(readOnly = true)
    public ClaimStatsResponse getClaimStats() {

        Map<String, Long> statusCounts =
                Arrays.stream(ClaimStatus.values())
                        .collect(
                                Collectors.toMap(
                                        Enum::name,
                                        claimRepository::countByStatus
                                )
                        );

        return new ClaimStatsResponse(
                claimRepository.count(),
                claimRepository.countByDuplicateSuspectedTrue(),
                statusCounts
        );
    }
}
package com.example.insurance.service;

import com.example.insurance.domain.claim.Claim;
import com.example.insurance.domain.claim.ClaimHistory;
import com.example.insurance.domain.claim.ClaimStatus;
import com.example.insurance.domain.claim.ClaimType;
import com.example.insurance.domain.claim.DocumentType;
import com.example.insurance.domain.contract.InsuranceContract;
import com.example.insurance.domain.customer.Customer;

import com.example.insurance.repository.ClaimHistoryRepository;
import com.example.insurance.repository.ClaimPaymentRepository;
import com.example.insurance.repository.ClaimRepository;
import com.example.insurance.repository.CustomerRepository;
import com.example.insurance.repository.InsuranceContractRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimServiceIntegrationTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InsuranceContractRepository contractRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimHistoryRepository historyRepository;

    @Autowired
    private ClaimPaymentRepository paymentRepository;

    private Long contractId;

    @BeforeEach
    void setUp() {

        Customer customer = new Customer(
                "테스트고객",
                LocalDate.of(1995, 1, 1),
                "010-1234-5678",
                "test@example.com"
        );

        customerRepository.save(customer);

        InsuranceContract contract =
                new InsuranceContract(
                        "POL-" + System.nanoTime(),
                        "테스트 건강보험",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        "테스트고객",
                        "테스트고객",
                        "테스트고객",
                        customer
                );

        contractRepository.save(contract);

        contractId = contract.getId();
    }

    @Test
    void 보험금청구가_정상적으로_생성되고_이력이_저장된다() {

        Long claimId = claimService.createClaim(
                contractId,
                ClaimType.DIAGNOSIS,
                LocalDate.of(2026, 9, 1),
                "질병 진단",
                new BigDecimal("500000")
        );

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        assertEquals(
                ClaimStatus.RECEIVED,
                claim.getStatus()
        );

        assertFalse(
                claim.isDuplicateSuspected()
        );

        List<ClaimHistory> histories =
                historyRepository
                        .findByClaimIdOrderByChangedAtAsc(
                                claimId
                        );

        assertEquals(
                1,
                histories.size()
        );

        assertNull(
                histories.get(0).getPreviousStatus()
        );

        assertEquals(
                ClaimStatus.RECEIVED,
                histories.get(0).getNewStatus()
        );
    }

    @Test
    void 동일계약_동일사고일_동일청구유형이면_중복청구의심으로_표시된다() {

        claimService.createClaim(
                contractId,
                ClaimType.ACCIDENT,
                LocalDate.of(2026, 9, 1),
                "교통사고 최초 청구",
                new BigDecimal("1000000")
        );

        Long secondClaimId = claimService.createClaim(
                contractId,
                ClaimType.ACCIDENT,
                LocalDate.of(2026, 9, 1),
                "교통사고 추가 청구",
                new BigDecimal("1000000")
        );

        Claim secondClaim = claimRepository
                .findById(secondClaimId)
                .orElseThrow();

        assertTrue(
                secondClaim.isDuplicateSuspected()
        );
    }

    @Test
    void 필수서류가_누락되면_추가서류요청상태로_변경된다() {

        Long claimId = claimService.createClaim(
                contractId,
                ClaimType.HOSPITALIZATION,
                LocalDate.of(2026, 9, 1),
                "입원 치료",
                new BigDecimal("1000000")
        );

        claimService.startDocumentReview(
                claimId,
                "reviewer01"
        );

        claimService.addDocument(
                claimId,
                DocumentType.MEDICAL_RECEIPT,
                "receipt.pdf"
        );

        claimService.validateDocuments(
                claimId,
                "reviewer01"
        );

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        assertEquals(
                ClaimStatus.ADDITIONAL_DOCUMENT_REQUIRED,
                claim.getStatus()
        );

        List<ClaimHistory> histories =
                historyRepository
                        .findByClaimIdOrderByChangedAtAsc(
                                claimId
                        );

        assertEquals(
                3,
                histories.size()
        );

        assertEquals(
                ClaimStatus.ADDITIONAL_DOCUMENT_REQUIRED,
                histories.get(2).getNewStatus()
        );
    }

    @Test
    void 필수서류검증부터_승인과_보험금지급까지_정상처리된다() {

        Long claimId = claimService.createClaim(
                contractId,
                ClaimType.HOSPITALIZATION,
                LocalDate.of(2026, 9, 1),
                "입원 치료",
                new BigDecimal("1000000")
        );

        claimService.startDocumentReview(
                claimId,
                "reviewer01"
        );

        claimService.addDocument(
                claimId,
                DocumentType.DIAGNOSIS_CERTIFICATE,
                "diagnosis.pdf"
        );

        claimService.addDocument(
                claimId,
                DocumentType.HOSPITALIZATION_CERTIFICATE,
                "hospitalization.pdf"
        );

        claimService.addDocument(
                claimId,
                DocumentType.MEDICAL_RECEIPT,
                "receipt.pdf"
        );

        claimService.validateDocuments(
                claimId,
                "reviewer01"
        );

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        assertEquals(
                ClaimStatus.UNDER_REVIEW,
                claim.getStatus()
        );

        claimService.approveClaim(
                claimId,
                "reviewer01",
                new BigDecimal("800000"),
                "지급 요건 충족"
        );

        assertEquals(
                ClaimStatus.APPROVED,
                claim.getStatus()
        );

        assertEquals(
                0,
                new BigDecimal("800000")
                        .compareTo(
                                claim.getApprovedAmount()
                        )
        );

        claimService.payClaim(
                claimId,
                "테스트은행",
                "123-****-456"
        );

        assertEquals(
                ClaimStatus.PAID,
                claim.getStatus()
        );

        assertNotNull(
                claim.getCompletedAt()
        );

        assertTrue(
                paymentRepository.existsByClaimId(
                        claimId
                )
        );

        List<ClaimHistory> histories =
                historyRepository
                        .findByClaimIdOrderByChangedAtAsc(
                                claimId
                        );

        assertEquals(
                5,
                histories.size()
        );

        assertEquals(
                ClaimStatus.RECEIVED,
                histories.get(0).getNewStatus()
        );

        assertEquals(
                ClaimStatus.DOCUMENT_REVIEW,
                histories.get(1).getNewStatus()
        );

        assertEquals(
                ClaimStatus.UNDER_REVIEW,
                histories.get(2).getNewStatus()
        );

        assertEquals(
                ClaimStatus.APPROVED,
                histories.get(3).getNewStatus()
        );

        assertEquals(
                ClaimStatus.PAID,
                histories.get(4).getNewStatus()
        );
    }
}
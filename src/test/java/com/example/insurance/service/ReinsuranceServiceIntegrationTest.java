package com.example.insurance.service;

import com.example.insurance.domain.claim.*;
import com.example.insurance.domain.contract.InsuranceContract;
import com.example.insurance.domain.customer.Customer;
import com.example.insurance.dto.ReinsuranceRecoveryResponse;
import com.example.insurance.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReinsuranceServiceIntegrationTest {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ReinsuranceService reinsuranceService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InsuranceContractRepository contractRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimPaymentRepository paymentRepository;

    @Autowired
    private ReinsuranceRecoveryRepository recoveryRepository;

    private Long contractId;

    @BeforeEach
    void setUp() {

        Customer customer = new Customer(
                "재보험테스트고객",
                LocalDate.of(1995, 1, 1),
                "010-1111-2222",
                "reinsurance@example.com"
        );

        customerRepository.save(customer);

        InsuranceContract contract =
                new InsuranceContract(
                        "POL-RE-" + System.nanoTime(),
                        "재보험 테스트 보험",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        "재보험테스트고객",
                        "재보험테스트고객",
                        "재보험테스트고객",
                        customer
                );

        contractRepository.save(contract);

        contractId = contract.getId();
    }

    @Test
    void QuotaShare_재보험회수액이_정상적으로_계산된다() {

        Long claimId = createApprovedClaim();

        claimService.payClaim(
                claimId,
                "테스트은행",
                "123-****-456"
        );

        reinsuranceService.createReinsuranceContract(
                contractId,
                "RE-QS-" + System.nanoTime(),
                "Korean Re",
                new BigDecimal("0.40"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        Long recoveryId =
                reinsuranceService.createRecovery(
                        claimId
                );

        assertNotNull(recoveryId);

        ReinsuranceRecoveryResponse response =
                reinsuranceService.getRecovery(
                        claimId
                );

        assertEquals(
                0,
                new BigDecimal("800000.00")
                        .compareTo(
                                response.grossAmount()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("320000.00")
                        .compareTo(
                                response.recoveryAmount()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("480000.00")
                        .compareTo(
                                response.netAmount()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("0.4000")
                        .compareTo(
                                response.cessionRate()
                        )
        );
    }

    @Test
    void 동일_보험금지급건의_재보험회수는_중복처리할수없다() {

        Long claimId = createApprovedClaim();

        claimService.payClaim(
                claimId,
                "테스트은행",
                "123-****-456"
        );

        reinsuranceService.createReinsuranceContract(
                contractId,
                "RE-QS-" + System.nanoTime(),
                "Korean Re",
                new BigDecimal("0.40"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        reinsuranceService.createRecovery(
                claimId
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                reinsuranceService
                                        .createRecovery(
                                                claimId
                                        )
                );

        assertEquals(
                "이미 재보험 회수 처리가 완료된 지급 건입니다.",
                exception.getMessage()
        );
    }

    @Test
    void 보험금지급이_완료되지않으면_재보험회수할수없다() {

        Long claimId = createApprovedClaim();

        Claim claim =
                claimRepository
                        .findById(claimId)
                        .orElseThrow();

        ClaimPayment payment =
                new ClaimPayment(
                        claim,
                        claim.getApprovedAmount(),
                        "테스트은행",
                        "123-****-456"
                );

        // complete()를 호출하지 않아 READY 상태 유지
        paymentRepository.save(payment);

        reinsuranceService.createReinsuranceContract(
                contractId,
                "RE-QS-" + System.nanoTime(),
                "Korean Re",
                new BigDecimal("0.40"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                reinsuranceService
                                        .createRecovery(
                                                claimId
                                        )
                );

        assertEquals(
                "보험금 지급이 완료된 청구만 재보험 회수 처리할 수 있습니다.",
                exception.getMessage()
        );
    }

    private Long createApprovedClaim() {

        Long claimId =
                claimService.createClaim(
                        contractId,
                        ClaimType.DIAGNOSIS,
                        LocalDate.of(2026, 9, 1),
                        "질병 진단",
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

        claimService.validateDocuments(
                claimId,
                "reviewer01"
        );

        claimService.approveClaim(
                claimId,
                "reviewer01",
                new BigDecimal("800000"),
                "지급 요건 충족"
        );

        return claimId;
    }
}
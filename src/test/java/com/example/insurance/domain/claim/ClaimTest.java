package com.example.insurance.domain.claim;

import com.example.insurance.domain.contract.InsuranceContract;
import com.example.insurance.domain.customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClaimTest {

    private InsuranceContract contract;

    @BeforeEach
    void setUp() {

        Customer customer = new Customer(
                "테스트고객",
                LocalDate.of(1995, 1, 1),
                "010-1234-5678",
                "test@example.com"
        );

        contract = new InsuranceContract(
                "POL-TEST-001",
                "테스트 건강보험",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "테스트고객",
                "테스트고객",
                "테스트고객",
                customer
        );
    }

    private Claim createClaim() {

        return new Claim(
                "CLM-TEST-001",
                contract,
                ClaimType.HOSPITALIZATION,
                LocalDate.of(2026, 9, 1),
                "입원 치료",
                new BigDecimal("1000000")
        );
    }

    @Test
    void 청구생성시_상태는_RECEIVED() {

        Claim claim = createClaim();

        Assertions.assertEquals(
                ClaimStatus.RECEIVED,
                claim.getStatus()
        );
    }

    @Test
    void 정상적인_서류검토_및_심사전이가_가능하다() {

        Claim claim = createClaim();

        claim.startDocumentReview();

        assertEquals(
                ClaimStatus.DOCUMENT_REVIEW,
                claim.getStatus()
        );

        claim.startReview();

        assertEquals(
                ClaimStatus.UNDER_REVIEW,
                claim.getStatus()
        );
    }

    @Test
    void RECEIVED에서_바로_지급심사를_시작할수없다() {

        Claim claim = createClaim();

        assertThrows(
                IllegalStateException.class,
                claim::startReview
        );
    }

    @Test
    void 승인금액은_청구금액을_초과할수없다() {

        Claim claim = createClaim();

        claim.startDocumentReview();
        claim.startReview();

        assertThrows(
                IllegalArgumentException.class,
                () -> claim.approve(
                        new BigDecimal("1500000")
                )
        );
    }

    @Test
    void 정상승인시_APPROVED로_변경된다() {

        Claim claim = createClaim();

        claim.startDocumentReview();
        claim.startReview();

        claim.approve(
                new BigDecimal("800000")
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
    }

    @Test
    void 승인된_청구만_지급완료_처리할수있다() {

        Claim claim = createClaim();

        assertThrows(
                IllegalStateException.class,
                claim::markAsPaid
        );

        claim.startDocumentReview();
        claim.startReview();
        claim.approve(
                new BigDecimal("800000")
        );
        claim.markAsPaid();

        assertEquals(
                ClaimStatus.PAID,
                claim.getStatus()
        );

        assertNotNull(
                claim.getCompletedAt()
        );
    }
}
package com.example.insurance.domain.contract;

import com.example.insurance.domain.customer.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceContractTest {

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

    @Test
    void 보험기간내_사고는_청구가능하다() {

        assertDoesNotThrow(
                () -> contract.validateClaimable(
                        LocalDate.of(2026, 9, 1)
                )
        );
    }

    @Test
    void 보험기간밖_사고는_청구할수없다() {

        assertThrows(
                IllegalArgumentException.class,
                () -> contract.validateClaimable(
                        LocalDate.of(2027, 1, 1)
                )
        );
    }
}
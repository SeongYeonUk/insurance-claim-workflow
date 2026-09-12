package com.example.insurance.repository;

import com.example.insurance.domain.contract.InsuranceContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsuranceContractRepository
        extends JpaRepository<InsuranceContract, Long> {

    Optional<InsuranceContract> findByContractNumber(String contractNumber);
}
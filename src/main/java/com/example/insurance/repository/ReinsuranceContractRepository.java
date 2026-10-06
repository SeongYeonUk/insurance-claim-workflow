package com.example.insurance.repository;

import com.example.insurance.domain.reinsurance.ReinsuranceContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReinsuranceContractRepository
        extends JpaRepository<ReinsuranceContract, Long> {

    Optional<ReinsuranceContract>
    findByInsuranceContractId(Long insuranceContractId);

    boolean existsByInsuranceContractId(
            Long insuranceContractId
    );
}
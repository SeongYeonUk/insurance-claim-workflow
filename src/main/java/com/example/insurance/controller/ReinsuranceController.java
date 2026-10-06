package com.example.insurance.controller;

import com.example.insurance.dto.CreateReinsuranceContractRequest;
import com.example.insurance.dto.CreateReinsuranceContractResponse;
import com.example.insurance.dto.CreateReinsuranceRecoveryResponse;
import com.example.insurance.dto.ReinsuranceRecoveryResponse;
import com.example.insurance.service.ReinsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reinsurance")
@RequiredArgsConstructor
public class ReinsuranceController {

    private final ReinsuranceService reinsuranceService;

    @PostMapping("/contracts")
    public ResponseEntity<CreateReinsuranceContractResponse>
    createReinsuranceContract(
            @Valid
            @RequestBody
            CreateReinsuranceContractRequest request
    ) {

        Long reinsuranceContractId =
                reinsuranceService
                        .createReinsuranceContract(
                                request.insuranceContractId(),
                                request.reinsuranceContractNumber(),
                                request.reinsurerName(),
                                request.cessionRate(),
                                request.startDate(),
                                request.endDate()
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new CreateReinsuranceContractResponse(
                                reinsuranceContractId
                        )
                );
    }

    @PostMapping("/recoveries/claims/{claimId}")
    public ResponseEntity<CreateReinsuranceRecoveryResponse>
    createRecovery(
            @PathVariable Long claimId
    ) {

        Long recoveryId =
                reinsuranceService
                        .createRecovery(claimId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new CreateReinsuranceRecoveryResponse(
                                recoveryId
                        )
                );
    }

    @GetMapping("/recoveries/claims/{claimId}")
    public ReinsuranceRecoveryResponse getRecovery(
            @PathVariable Long claimId
    ) {

        return reinsuranceService
                .getRecovery(claimId);
    }
}
package com.example.insurance.domain.claim;

import com.example.insurance.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "claim_documents")
public class ClaimDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    public ClaimDocument(
            Claim claim,
            DocumentType documentType,
            String fileName
    ) {
        this.claim = claim;
        this.documentType = documentType;
        this.fileName = fileName;
        this.submittedAt = LocalDateTime.now();
    }
}
package com.example.insurance.domain.customer;

import com.example.insurance.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "customers")
public class Customer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private LocalDate birthDate;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String email;

    public Customer(
            String name,
            LocalDate birthDate,
            String phone,
            String email
    ) {
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
    }
}
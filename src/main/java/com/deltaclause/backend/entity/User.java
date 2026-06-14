package com.deltaclause.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String role; // "student" or "admin"

    @Column(nullable = false)
    private int rewardPoints;

    private String referredBy; // tracks email of referrer

    @Column(nullable = false)
    private int referralCount;

    @Column(nullable = false)
    private boolean refundEligible;
}

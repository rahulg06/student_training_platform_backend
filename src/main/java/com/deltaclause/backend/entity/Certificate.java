package com.deltaclause.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false)
    private String id; // e.g., "DC-INT-10254"

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private String internshipId;

    @Column(nullable = false)
    private String internshipName;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private String issuedAt;

    @Column(nullable = false)
    private String digitalSignature; // Cryptographic SHA validation hash
}

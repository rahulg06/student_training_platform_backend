package com.deltaclause.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userPhone;

    @Column(nullable = false)
    private String internshipId;

    @Column(nullable = false)
    private String internshipName;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private double pricePaid;

    @Column(nullable = false)
    private String paymentTxId;

    @Column(columnDefinition = "LONGTEXT")
    private String paymentScreenshot; // Handles standard base64 strings cleanly

    @Column(nullable = false)
    private String status; // "pending", "approved", "rejected"

    @Column(nullable = false)
    private String enrolledAt;

    private String approvedAt;

    private String referralCodeUsed;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<UserTaskSubmission> tasks = new ArrayList<>();

    private String certificateId;
}

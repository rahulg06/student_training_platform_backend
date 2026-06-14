package com.deltaclause.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "recipient_to", nullable = false)
    private String to;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    private String attachmentName;

    @Column(nullable = false)
    private String sentAt;
}

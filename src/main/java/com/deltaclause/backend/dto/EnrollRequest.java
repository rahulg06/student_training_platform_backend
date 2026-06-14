package com.deltaclause.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollRequest {
    private String internshipId;
    private String paymentTxId;
    private String paymentScreenshot; // base64 string
    private String referralCodeUsed;
    private boolean isPointsUsed;
}

package com.deltaclause.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private String taskId;
    private String status; // "approved" or "needs_work"
    private String feedback;
}

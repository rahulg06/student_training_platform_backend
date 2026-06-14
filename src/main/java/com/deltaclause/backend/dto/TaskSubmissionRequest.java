package com.deltaclause.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionRequest {
    private String taskId;
    private String submittedText;
    private String submissionUrl;
}

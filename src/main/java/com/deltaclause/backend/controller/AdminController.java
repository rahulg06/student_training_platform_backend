package com.deltaclause.backend.controller;

import com.deltaclause.backend.dto.*;
import com.deltaclause.backend.entity.*;
import com.deltaclause.backend.repository.UserRepository;
import com.deltaclause.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    @GetMapping("/api/admin/enrollments")
    public ResponseEntity<List<Enrollment>> getEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @PostMapping("/api/admin/enrollments/{id}/verify-payment")
    public ResponseEntity<?> verifyPayment(@PathVariable String id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        if (action == null) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Action is required (approve/reject)"));
        }
        try {
            Enrollment enrollment = enrollmentService.verifyPayment(id, action);
            return ResponseEntity.ok(enrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/enrollments/{enrollId}/review-task")
    public ResponseEntity<?> reviewTask(@PathVariable String enrollId, @RequestBody ReviewRequest req) {
        try {
            Enrollment enrollment = enrollmentService.reviewTask(enrollId, req);
            return ResponseEntity.ok(enrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/api/admin/emails-sent")
    public ResponseEntity<List<EmailLog>> getEmails() {
        return ResponseEntity.ok(enrollmentService.getEmailLogs());
    }

    @GetMapping("/api/admin/stats")
    public ResponseEntity<?> getStats() {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
        long totalStudents = userRepository.findAll().stream().filter(u -> "student".equalsIgnoreCase(u.getRole())).count();
        long totalEnrollments = enrollments.size();
        long pendingEnrollments = enrollments.stream().filter(e -> "pending".equalsIgnoreCase(e.getStatus())).count();
        
        double totalEarnings = enrollments.stream()
                .filter(e -> "approved".equalsIgnoreCase(e.getStatus()))
                .mapToDouble(Enrollment::getPricePaid)
                .sum();

        // Count certificates
        long certificateCount = enrollments.stream()
                .filter(e -> e.getCertificateId() != null)
                .count();

        Map<String, Object> statsObj = new HashMap<>();
        statsObj.put("totalStudents", totalStudents);
        statsObj.put("totalEnrollments", totalEnrollments);
        statsObj.put("pendingEnrollments", pendingEnrollments);
        statsObj.put("totalEarnings", totalEarnings);
        statsObj.put("certificateCount", certificateCount);

        return ResponseEntity.ok(statsObj);
    }

    @GetMapping("/api/config/referrals")
    @PreAuthorize("permitAll()") // Let public read threshold
    public ResponseEntity<?> getReferralsConfig() {
        Map<String, Integer> res = new HashMap<>();
        res.put("referralsNeededForRefund", enrollmentService.getReferralThreshold());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/api/config/referrals")
    public ResponseEntity<?> setReferralsConfig(@RequestBody Map<String, Integer> body) {
        Integer threshold = body.get("referralsNeededForRefund");
        if (threshold == null || threshold < 1) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Threshold value must be greater than or equal to 1."));
        }
        enrollmentService.setReferralThreshold(threshold);
        return ResponseEntity.ok(body);
    }
}

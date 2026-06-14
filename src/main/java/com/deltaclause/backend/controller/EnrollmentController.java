package com.deltaclause.backend.controller;

import com.deltaclause.backend.dto.*;
import com.deltaclause.backend.entity.Enrollment;
import com.deltaclause.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody EnrollRequest req) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Enrollment enrollment = enrollmentService.apply(email, req);
            return ResponseEntity.status(201).body(enrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Enrollment>> getMyEnrollments() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(email));
    }

    @PostMapping("/{enrollId}/submit-task")
    public ResponseEntity<?> submitTask(@PathVariable String enrollId, @RequestBody TaskSubmissionRequest req) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Enrollment enrollment = enrollmentService.submitTask(email, enrollId, req);
            return ResponseEntity.ok(enrollment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }
}

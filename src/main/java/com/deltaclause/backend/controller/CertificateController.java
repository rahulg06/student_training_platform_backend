package com.deltaclause.backend.controller;

import com.deltaclause.backend.entity.Certificate;
import com.deltaclause.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/verify/{id}")
    public ResponseEntity<?> verify(@PathVariable String id) {
        Optional<Certificate> certOpt = enrollmentService.verifyCertificate(id);
        
        Map<String, Object> response = new HashMap<>();
        if (certOpt.isEmpty()) {
            response.put("success", false);
            response.put("error", "Verification failed. Certificate ID is invalid or cannot be found.");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("success", true);
        response.put("certificate", certOpt.get());
        return ResponseEntity.ok(response);
    }
}

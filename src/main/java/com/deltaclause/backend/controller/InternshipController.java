package com.deltaclause.backend.controller;

import com.deltaclause.backend.entity.Internship;
import com.deltaclause.backend.service.InternshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    @GetMapping
    public ResponseEntity<List<Internship>> getInternships() {
        return ResponseEntity.ok(internshipService.getAllInternships());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Internship> create(@RequestBody Internship internship) {
        return ResponseEntity.status(201).body(internshipService.createInternship(internship));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Internship> update(@PathVariable String id, @RequestBody Internship internship) {
        return internshipService.updateInternship(id, internship)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        boolean deleted = internshipService.deleteInternship(id);
        if (deleted) {
            return ResponseEntity.ok(java.util.Collections.singletonMap("success", true));
        }
        return ResponseEntity.notFound().build();
    }
}

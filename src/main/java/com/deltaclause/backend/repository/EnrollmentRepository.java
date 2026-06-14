package com.deltaclause.backend.repository;

import com.deltaclause.backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    List<Enrollment> findByUserEmailIgnoreCase(String email);
    List<Enrollment> findByStatus(String status);
}

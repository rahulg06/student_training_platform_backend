package com.deltaclause.backend.repository;

import com.deltaclause.backend.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, String> {
    List<EmailLog> findAllByOrderBySentAtDesc();
}

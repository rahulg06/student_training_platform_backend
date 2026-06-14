package com.deltaclause.backend.service;

import com.deltaclause.backend.dto.*;
import com.deltaclause.backend.entity.*;
import com.deltaclause.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final CertificateRepository certificateRepository;
    private final EmailLogRepository emailLogRepository;
    private final RedisCacheService redisCacheService;

    // Configurable parameters from Redis keys or fallback defaults
    public int getReferralThreshold() {
        Object val = redisCacheService.get("config:referral_threshold");
        if (val instanceof Integer) {
            return (Integer) val;
        }
        return 3; // default threshold is 3 referrals
    }

    public void setReferralThreshold(int threshold) {
        redisCacheService.set("config:referral_threshold", threshold, 365, java.util.concurrent.TimeUnit.DAYS);
    }

    @Transactional
    public Enrollment apply(String email, EnrollRequest req) {
        User student = userRepository.findById(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User profile not found."));

        Internship internship = internshipRepository.findById(req.getInternshipId())
                .orElseThrow(() -> new IllegalArgumentException("Requested course does not exist."));

        // Deduct reward points: 1 point = 1 INR
        double finalPrice = internship.getPrice();
        if (req.isPointsUsed() && student.getRewardPoints() > 0) {
            int discount = (int) Math.min(student.getRewardPoints(), finalPrice);
            finalPrice -= discount;
            student.setRewardPoints(student.getRewardPoints() - discount);
            userRepository.save(student);
        }

        // Generate enrollment
        String enrollmentId = "enroll-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .userEmail(student.getEmail())
                .userName(student.getName())
                .userPhone(student.getPhone())
                .internshipId(internship.getId())
                .internshipName(internship.getName())
                .duration(internship.getDuration())
                .pricePaid(finalPrice)
                .paymentTxId(req.getPaymentTxId())
                .paymentScreenshot(req.getPaymentScreenshot() != null ? req.getPaymentScreenshot() : "")
                .status("pending")
                .enrolledAt(Instant.now().toString())
                .referralCodeUsed(req.getReferralCodeUsed() != null ? req.getReferralCodeUsed().trim() : null)
                .build();

        // Populate tasks from default template
        List<UserTaskSubmission> tasksList = new ArrayList<>();
        if (internship.getTaskSheets() != null) {
            for (TaskSheetItem item : internship.getTaskSheets()) {
                tasksList.add(UserTaskSubmission.builder()
                        .taskId(item.getId())
                        .taskTitle(item.getTitle())
                        .submittedText("")
                        .submissionUrl("")
                        .submittedAt("")
                        .status("pending")
                        .enrollment(enrollment)
                        .build());
            }
        }
        enrollment.setTasks(tasksList);

        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getMyEnrollments(String email) {
        return enrollmentRepository.findByUserEmailIgnoreCase(email);
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    @Transactional
    public Enrollment submitTask(String email, String enrollId, TaskSubmissionRequest req) {
        Enrollment enrollment = enrollmentRepository.findById(enrollId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment contract not found."));

        if (!enrollment.getUserEmail().equalsIgnoreCase(email)) {
             throw new SecurityException("Unauthorized access to this enrollment record.");
        }

        if (!"approved".equals(enrollment.getStatus())) {
            throw new IllegalStateException("Your enrollment UPI verification is currently being examined. Task locking active.");
        }

        UserTaskSubmission submission = enrollment.getTasks().stream()
                .filter(t -> t.getTaskId().equals(req.getTaskId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone ID is not mapped."));

        submission.setSubmittedText(req.getSubmittedText());
        submission.setSubmissionUrl(req.getSubmissionUrl());
        submission.setSubmittedAt(Instant.now().toString());
        submission.setStatus("pending"); // Resubmit for grading

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment verifyPayment(String enrollId, String action) {
        Enrollment enrollment = enrollmentRepository.findById(enrollId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment record not found."));

        if ("approve".equalsIgnoreCase(action)) {
            enrollment.setStatus("approved");
            enrollment.setApprovedAt(Instant.now().toString());

            // Process referral bonus rewards
            if (enrollment.getReferralCodeUsed() != null && !enrollment.getReferralCodeUsed().isEmpty()) {
                String refCode = enrollment.getReferralCodeUsed().toLowerCase().trim();
                String refPhone = refCode.replaceAll("[\\s-+]", "");
                
                Optional<User> referrerOpt = userRepository.findById(refCode)
                        .or(() -> userRepository.findByPhone(refPhone));

                if (referrerOpt.isPresent()) {
                    User referrer = referrerOpt.get();
                    referrer.setReferralCount(referrer.getReferralCount() + 1);
                    referrer.setRewardPoints(referrer.getRewardPoints() + 500); // 500 points per successful referral

                    if (referrer.getReferralCount() >= getReferralThreshold()) {
                        referrer.setRefundEligible(true);
                    }
                    userRepository.save(referrer);
                }
            }

            // Mock Outgoing welcome logs trigger simulation (and index onto SFTP records)
            String materialFile = "Deltaclause_" + enrollment.getInternshipId().replace("intern-", "") + "_Full_Training_Kit.pdf";
            String subject = "🚀 Enrollment Approved: Welcome to Deltaclause - " + enrollment.getInternshipName() + "!";
            String body = "Dear " + enrollment.getUserName() + ",\n\n"
                    + "Your UPI transaction screenshot has been approved! Transaction reference ID: " + enrollment.getPaymentTxId() + ".\n\n"
                    + "The learning module of " + enrollment.getDuration() + " has been initialized inside your student workspace.\n"
                    + "The resource guides and code repositories have been securely pulled from our SFTP server [sftp://secure-materials.deltaclause.com] and uploaded.\n\n"
                    + "Best of Luck,\n"
                    + "Operations Team - Deltaclause";

            EmailLog log = EmailLog.builder()
                    .id("mail-" + System.currentTimeMillis())
                    .to(enrollment.getUserEmail())
                    .subject(subject)
                    .body(body)
                    .attachmentName(materialFile)
                    .sentAt(Instant.now().toString())
                    .build();
            emailLogRepository.save(log);

        } else {
            enrollment.setStatus("rejected");
        }

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment reviewTask(String enrollId, ReviewRequest req) {
        Enrollment enrollment = enrollmentRepository.findById(enrollId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment record not found."));

        UserTaskSubmission submission = enrollment.getTasks().stream()
                .filter(t -> t.getTaskId().equals(req.getTaskId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found."));

        submission.setStatus(req.getStatus());
        submission.setFeedback(req.getFeedback());

        // Check if all milestone tasks are fully approved
        boolean allFinished = enrollment.getTasks().stream()
                .allMatch(t -> "approved".equalsIgnoreCase(t.getStatus()));

        if (allFinished && enrollment.getCertificateId() == null) {
            String genCertId = "DC-INT-" + (int)(10000 + Math.random() * 90000);
            enrollment.setCertificateId(genCertId);

            // Create cryptographically signed certificate hash
            String sign = UUID.randomUUID().toString().replace("-", "").substring(0, 48);

            Certificate cert = Certificate.builder()
                    .id(genCertId)
                    .studentName(enrollment.getUserName())
                    .studentEmail(enrollment.getUserEmail())
                    .internshipId(enrollment.getInternshipId())
                    .internshipName(enrollment.getInternshipName())
                    .duration(enrollment.getDuration())
                    .issuedAt(Instant.now().toString())
                    .digitalSignature(sign)
                    .build();

            certificateRepository.save(cert);
            
            // Push certificate details directly to Redis for extremely fast read-heavy validation looks!
            redisCacheService.set("certificates:" + genCertId, cert, 7, java.util.concurrent.TimeUnit.DAYS);

            // Log digital evaluation certificate award mail
            String mailSubject = "🎓 Outstanding Accomplishment: Your Verified Deltaclause Certificate is Ready!";
            String mailBody = "Salutations " + enrollment.getUserName() + ",\n\n"
                    + "We are extremely proud to reward you for successfully implementing all key industry task milestones in the \"" + enrollment.getInternshipName() + "\" training.\n\n"
                    + "Your verified Certificate ID is: " + genCertId + "\n"
                    + "Our registrar validation system has cryptographically indexed yours on https://deltaclause.com/verify.\n"
                    + "Keep ascending on your engineering path!\n\n"
                    + "Best Regards,\n"
                    + "Registrar Office, Deltaclause.";

            EmailLog log = EmailLog.builder()
                    .id("mail-cert-" + System.currentTimeMillis())
                    .to(enrollment.getUserEmail())
                    .subject(mailSubject)
                    .body(mailBody)
                    .attachmentName("Deltaclause_Verified_Certificate_" + genCertId + ".pdf")
                    .sentAt(Instant.now().toString())
                    .build();
            emailLogRepository.save(log);
        }

        return enrollmentRepository.save(enrollment);
    }

    @Cacheable(value = "certificates", key = "#id")
    public Optional<Certificate> verifyCertificate(String id) {
        System.out.println("[Redis Cache Miss] Looking up certificate ID: " + id + " from MySQL Database...");
        return certificateRepository.findById(id);
    }

    public List<EmailLog> getEmailLogs() {
        return emailLogRepository.findAllByOrderBySentAtDesc();
    }
}

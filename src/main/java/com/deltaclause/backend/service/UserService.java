package com.deltaclause.backend.service;

import com.deltaclause.backend.dto.*;
import com.deltaclause.backend.entity.User;
import com.deltaclause.backend.repository.UserRepository;
import com.deltaclause.backend.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest req) {
        String emailTrim = req.getEmail().trim().toLowerCase();
        String phoneClean = req.getPhone().replaceAll("[\\s-+]", "");

        if (userRepository.existsById(emailTrim)) {
            return AuthResponse.builder().success(false).error("User with this email already exists.").build();
        }
        if (userRepository.existsByPhone(phoneClean)) {
            return AuthResponse.builder().success(false).error("User with this phone number already exists.").build();
        }

        // Detect referrer by email or phone
        Optional<User> referrerOpt = Optional.empty();
        if (req.getReferralCode() != null && !req.getReferralCode().trim().isEmpty()) {
            String refCode = req.getReferralCode().trim().toLowerCase();
            String refPhone = req.getReferralCode().replaceAll("[\\s-+]", "");
            referrerOpt = userRepository.findById(refCode)
                    .or(() -> userRepository.findByPhone(refPhone));
        }

        User user = User.builder()
                .email(emailTrim)
                .name(req.getName().trim())
                .phone(phoneClean)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("student")
                .rewardPoints(referrerOpt.isPresent() ? 150 : 0) // Reward points for referree
                .referredBy(referrerOpt.map(User::getEmail).orElse(null))
                .referralCount(0)
                .refundEligible(false)
                .build();

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail(), user.getName(), user.getPhone(), user.getRole());

        // Clear password hash before returning user object in response
        user.setPasswordHash(null);

        return AuthResponse.builder()
                .success(true)
                .user(user)
                .accessToken(token)
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        String emailTrim = req.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findById(emailTrim);

        if (userOpt.isEmpty() || !passwordEncoder.matches(req.getPassword(), userOpt.get().getPasswordHash())) {
            return AuthResponse.builder().success(false).error("Invalid email or password.").build();
        }

        User user = userOpt.get();
        String token = jwtUtils.generateToken(user.getEmail(), user.getName(), user.getPhone(), user.getRole());

        user.setPasswordHash(null);

        return AuthResponse.builder()
                .success(true)
                .user(user)
                .accessToken(token)
                .build();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findById(email.trim().toLowerCase())
                .map(u -> {
                    u.setPasswordHash(null);
                    return u;
                });
    }
}

package com.deltaclause.backend.controller;

import com.deltaclause.backend.dto.*;
import com.deltaclause.backend.entity.User;
import com.deltaclause.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        AuthResponse res = userService.register(req);
        if (!res.isSuccess()) {
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        AuthResponse res = userService.login(req);
        if (!res.isSuccess()) {
            return ResponseEntity.status(401).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "User not found"));
        }
        
        return ResponseEntity.ok(java.util.Collections.singletonMap("user", userOpt.get()));
    }
}

// Small helper subclass since Collections was not imported
class Collections {
    public static <K, V> java.util.Map<K, V> singletonMap(K key, V value) {
        return java.util.Collections.singletonMap(key, value);
    }
}

package com.deltaclause.backend.dto;

import com.deltaclause.backend.entity.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private boolean success;
    private User user;
    private String accessToken;
    private String error;
}

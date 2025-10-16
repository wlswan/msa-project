package com.example.auth_service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private String role;
}

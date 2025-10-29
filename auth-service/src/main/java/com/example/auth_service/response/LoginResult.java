package com.example.auth_service.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private String username;
    private String name;
    private String accessToken;
    private String refreshToken;
}

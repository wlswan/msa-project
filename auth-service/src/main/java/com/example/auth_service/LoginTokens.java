package com.example.auth_service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginTokens {
    private String accessToken;
    private String refreshToken;

}

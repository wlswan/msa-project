package com.example.auth_service.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String username;
    private String name;
}

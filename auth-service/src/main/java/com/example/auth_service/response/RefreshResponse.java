package com.example.auth_service.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshResponse {
    private String username;
    private String name;
}

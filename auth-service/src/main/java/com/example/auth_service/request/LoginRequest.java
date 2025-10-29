package com.example.auth_service.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "아이디는 필수 입력입니다.")
    private String username;
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    private String password;
}

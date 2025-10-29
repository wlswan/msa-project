package com.example.auth_service.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "아이디 입력은 필수입니다.")
    private String username;

    @NotBlank(message = "이름 입력은 필수입니다.")
    private String name;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Size(min = 8,message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
}

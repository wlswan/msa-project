package com.example.auth_service;


import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String name;
    private String password;
}

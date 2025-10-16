package com.example.board_service.dto;

import lombok.Data;

@Data
public class AuthValidateResponse {
    private boolean success;
    private String message;
    private AuthValidateData data;
}

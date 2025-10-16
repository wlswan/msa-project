package com.example.board_service.dto;

import lombok.Data;

@Data
public class AuthValidateData {
    private boolean valid;
    private String username;
    private String role;
}

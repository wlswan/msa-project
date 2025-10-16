package com.example.board_service;

import com.example.board_service.dto.AuthValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(
        name = "auth-service",
        url = "http://localhost:8080/auth"
)
public interface AuthClient {

    @GetMapping("/validate")
    AuthValidateResponse validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token);
}

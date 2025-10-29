package com.example.auth_service;

import com.example.auth_service.exception.MissingTokenException;
import com.example.auth_service.request.LoginRequest;
import com.example.auth_service.request.RegisterRequest;
import com.example.auth_service.response.*;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
            User user = authService.register(request);
            RegisterResponse response = RegisterResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .build();

            return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
            LoginResult loginResult = authService.login(request);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResult.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .name(loginResult.getName())
                    .username(loginResult.getUsername())
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION,"Bearer " + loginResult.getAccessToken())
                    .header(HttpHeaders.SET_COOKIE,cookie.toString())
                    .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken",required = false) String refreshToken) {
        if(refreshToken == null) {
            throw new MissingTokenException();
        }

        RefreshResult refreshResult = authService.refreshAccessToken(refreshToken);
        RefreshResponse response = RefreshResponse.builder()
                .username(refreshResult.getUsername())
                .name(refreshResult.getName())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshResult.getAccessToken())
                .body(response);
    }

}

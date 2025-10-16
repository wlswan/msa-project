package com.example.auth_service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            RegisterResponse response = RegisterResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .build();

            return ResponseEntity.ok(ApiResponse.<RegisterResponse>builder()
                    .success(true)
                    .message("회원가입 성공")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<RegisterResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
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
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(true)
                            .message("로그인 성공")
                            .data(response)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<LoginResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validate(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {

        if (tokenHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<TokenValidationResponse>builder()
                            .success(false)
                            .message("Authorization 헤더가 없습니다.")
                            .data(TokenValidationResponse.builder()
                                    .valid(false)
                                    .build())
                            .build());
        }

        try {
            String token = tokenHeader.replace("Bearer ", "");
            boolean isValid = authService.validate(token);

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<TokenValidationResponse>builder()
                                .success(false)
                                .message("유효하지 않은 토큰입니다.")
                                .data(TokenValidationResponse.builder()
                                        .valid(false)
                                        .build())
                                .build());
            }

            String username = authService.getUsernameFromToken(token);
            String role = authService.getRoleFromToken(token);

            return ResponseEntity.ok(ApiResponse.<TokenValidationResponse>builder()
                    .success(true)
                    .message("토큰이 유효합니다.")
                    .data(TokenValidationResponse.builder()
                            .valid(true)
                            .username(username)
                            .role(role)
                            .build())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<TokenValidationResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(TokenValidationResponse.builder()
                                    .valid(false)
                                    .build())
                            .build());
        }
    }


}

package com.example.auth_service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }
        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        return userRepository.save(user);
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.generateToken(user.getUsername(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        return LoginResult.builder()
                .username(user.getUsername())
                .name(user.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    public boolean validate(String token) {
        return jwtProvider.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtProvider.getUsernameFromToken(token);
    }

    public String getRoleFromToken(String token) {
        return jwtProvider.getRoleFromToken(token);
    }
}

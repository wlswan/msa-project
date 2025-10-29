package com.example.auth_service.service;

import com.example.auth_service.Role;
import com.example.auth_service.User;
import com.example.auth_service.UserRepository;
import com.example.auth_service.exception.DuplicateUsernameException;
import com.example.auth_service.exception.InvalidPasswordException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.request.LoginRequest;
import com.example.auth_service.request.RegisterRequest;
import com.example.auth_service.response.LoginResult;
import com.example.auth_service.response.RefreshResult;
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
            throw new DuplicateUsernameException();
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
                .orElseThrow(UserNotFoundException::new);

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new InvalidPasswordException();
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

    public RefreshResult refreshAccessToken(String refreshToken) {
        jwtProvider.validate(refreshToken);

        String username = jwtProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Role role = user.getRole();
        String accessToken = jwtProvider.generateToken(username, role);

        return RefreshResult.builder()
                .username(username)
                .name(user.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


}

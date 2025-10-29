package com.example.auth_service.service;

import com.example.auth_service.Role;
import com.example.auth_service.exception.ExpiredTokenException;
import com.example.auth_service.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    private static final String SECRET = "mysecretkeymysecretkeymysecretkeymysecretkey"; // 32자 이상 필수
    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60;       // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24; // 1일

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String username, Role role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String refreshToken) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject();
    }

    public void validate(String refreshToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken);

            Claims body = claims.getBody();

            if (body.getExpiration().before(new Date())) {
                throw new ExpiredTokenException();
            }
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }
}

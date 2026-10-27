package com.sujeongring.global.jwt;

import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId) {
        return createToken(
                userId,
                "ACCESS",
                accessTokenExpiration
        );
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId) {
        return createToken(
                userId,
                "REFRESH",
                refreshTokenExpiration
        );
    }

    /**
     * JWT 생성
     */
    private String createToken(
            Long userId,
            String tokenType,
            long expiration
    ) {
        Date now = new Date();
        Date expiryDate = new Date(
                now.getTime() + expiration
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (ExpiredJwtException e) {
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JWT에서 userId 추출
     */
    public Long getUserId(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Refresh Token 유효성 및 만료 여부 검증
     */
    public void validateRefreshToken(String token) {

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType =
                    claims.get("type", String.class);

            if (!"REFRESH".equals(tokenType)) {
                throw new BaseException(
                        AuthErrorCode.INVALID_REFRESH_TOKEN
                );
            }

        } catch (ExpiredJwtException e) {
            throw new BaseException(
                    AuthErrorCode.EXPIRED_REFRESH_TOKEN
            );

        } catch (BaseException e) {
            throw e;

        } catch (JwtException | IllegalArgumentException e) {
            throw new BaseException(
                    AuthErrorCode.INVALID_REFRESH_TOKEN
            );
        }
    }
}
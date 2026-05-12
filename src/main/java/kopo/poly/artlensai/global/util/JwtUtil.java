package kopo.poly.artlensai.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔑 환경변수로 시크릿 키 주입 (Base64 인코딩 권장)
    @Value("${jwt.secret:}")
    private String secretKey;

    @Value("${jwt.access-token-validity:3600000}") // 기본 1시간
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity:604800000}") // 기본 7일
    private long refreshTokenValidity;

    private Key key;

    @PostConstruct
    public void init() {
        // secretKey가 비어있거나 너무 짧으면 안전하게 HS256용 키 생성
        if (secretKey == null || secretKey.length() < 32) {
            key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            String generatedKey = Base64.getEncoder().encodeToString(key.getEncoded());
            System.out.println("Generated secure JWT key: " + generatedKey);
        } else {
            // Base64 디코딩 가능하게 저장했다면 decode
            try {
                key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
            } catch (IllegalArgumentException e) {
                // Base64로 인코딩 안 된 경우 그냥 바이트로 처리
                key = Keys.hmacShaKeyFor(secretKey.getBytes());
            }
        }
    }

    // ==================== 토큰 생성 ====================
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==================== 토큰 검증 ====================
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료됨: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("토큰 변조됨: " + e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // ==================== 토큰에서 정보 추출 ====================
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}
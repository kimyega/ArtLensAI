package kopo.poly.artlensai.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long REFRESH_TOKEN_EXPIRE =
            7 * 24 * 60 * 60;

    private static final long PASSWORD_RESET_EXPIRE =
            10 * 60;

    private static final long EMAIL_CODE_EXPIRE =
            3 * 60;

    // RefreshToken 저장
    public void saveRefreshToken(
            String email,
            String refreshToken
    ) {

        redisTemplate.opsForValue().set(
                "refresh:" + email,
                refreshToken,
                REFRESH_TOKEN_EXPIRE,
                TimeUnit.SECONDS
        );
    }

    // RefreshToken 조회
    public String getRefreshToken(String email) {

        Object value = redisTemplate.opsForValue()
                .get("refresh:" + email);

        return value != null ? value.toString() : null;
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(String email) {

        redisTemplate.delete("refresh:" + email);
    }


    public void savePasswordResetToken(
            String email,
            String token
    ) {

        redisTemplate.opsForValue().set(
                "password-reset:" + email,
                token,
                PASSWORD_RESET_EXPIRE,
                TimeUnit.SECONDS
        );
    }

    public String getPasswordResetToken(
            String email
    ) {

        Object value = redisTemplate.opsForValue()
                .get("password-reset:" + email);

        return value != null
                ? value.toString()
                : null;
    }

    public void deletePasswordResetToken(
            String email
    ) {

        redisTemplate.delete(
                "password-reset:" + email
        );
    }

    // 인증번호 저장
    public void saveEmailCode(
            String email,
            String code
    ) {

        redisTemplate.opsForValue().set(
                "email-code:" + email,
                code,
                EMAIL_CODE_EXPIRE,
                TimeUnit.SECONDS
        );
    }

    // 인증번호 조회
    public String getEmailCode(
            String email
    ) {

        Object value = redisTemplate.opsForValue()
                .get("email-code:" + email);

        return value != null
                ? value.toString()
                : null;
    }

    // 인증번호 삭제
    public void deleteEmailCode(
            String email
    ) {

        redisTemplate.delete(
                "email-code:" + email
        );
    }

    // 인증 완료 저장
    public void saveEmailVerified(
            String email
    ) {

        redisTemplate.opsForValue().set(
                "email-verified:" + email,
                "Y",
                30,
                TimeUnit.MINUTES
        );
    }

    // 인증 완료 여부 확인
    public boolean isEmailVerified(
            String email
    ) {

        return redisTemplate.hasKey(
                "email-verified:" + email
        );
    }

    public void deleteEmailVerified(
            String email
    ) {

        redisTemplate.delete(
                "email-verified:" + email
        );
    }
}
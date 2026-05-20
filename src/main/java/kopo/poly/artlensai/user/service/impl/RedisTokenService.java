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
}
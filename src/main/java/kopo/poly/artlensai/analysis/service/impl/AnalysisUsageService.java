package kopo.poly.artlensai.analysis.service.impl;

import kopo.poly.artlensai.analysis.dto.AnalysisUsageDTO;
import kopo.poly.artlensai.analysis.exception.AnalysisLimitExceededException;
import kopo.poly.artlensai.analysis.service.IAnalysisUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisUsageService implements IAnalysisUsageService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${analysis.daily-limit:20}")
    private int dailyLimit;

    @Override
    public AnalysisUsageDTO getUsage(Long userId) {

        return toDTO(currentUsage(userId));
    }

    @Override
    public int getRemaining(Long userId) {

        return toDTO(currentUsage(userId)).remaining();
    }

    @Override
    public void consumeOrThrow(Long userId) {

        String key = usageKey(userId);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {

            redisTemplate.expire(key, secondsUntilMidnight());
        }

        if (count != null && count > dailyLimit) {

            redisTemplate.opsForValue().decrement(key);

            log.info("일일 분석 한도 초과: userId={}, limit={}", userId, dailyLimit);

            throw new AnalysisLimitExceededException(toDTO(dailyLimit));
        }
    }

    private int currentUsage(Long userId) {

        Object value = redisTemplate.opsForValue().get(usageKey(userId));

        if (value == null) {

            return 0;
        }

        return Integer.parseInt(value.toString());
    }

    private AnalysisUsageDTO toDTO(int used) {

        return AnalysisUsageDTO.builder()
                .used(used)
                .remaining(Math.max(0, dailyLimit - used))
                .limit(dailyLimit)
                .build();
    }

    private String usageKey(Long userId) {

        return "analysis-usage:" + userId + ":" + LocalDate.now(KST);
    }

    private Duration secondsUntilMidnight() {

        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();

        return Duration.between(now, midnight);
    }
}

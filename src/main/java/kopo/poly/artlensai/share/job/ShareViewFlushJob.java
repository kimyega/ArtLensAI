package kopo.poly.artlensai.share.job;

import kopo.poly.artlensai.share.domain.postgres.ShareLinkRepository;
import kopo.poly.artlensai.share.domain.postgres.entity.ShareLinkEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 공유 페이지 조회수는 조회할 때마다 Redis에 INCR만 해두고,
 * 주기적으로 여기서 Postgres view_count에 누적 반영 후 Redis 키를 지운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShareViewFlushJob {

    private static final String KEY_PREFIX = "share-view:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ShareLinkRepository shareLinkRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void flush() {

        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {

            return;
        }

        for (String key : keys) {

            flushOne(key);
        }
    }

    private void flushOne(String key) {

        // getAndDelete로 원자적으로 꺼내 와야 그 사이에 들어온 조회(INCR)를 놓치지 않는다
        Object value = redisTemplate.opsForValue().getAndDelete(key);

        if (value == null) {

            return;
        }

        long delta = Long.parseLong(value.toString());

        String token = key.substring(KEY_PREFIX.length());

        shareLinkRepository.findByToken(token)
                .ifPresent(entity -> {

                    entity.setViewCount(entity.getViewCount() + delta);

                    shareLinkRepository.save(entity);
                });

        log.info("공유 조회수 반영: token={}, delta={}", token, delta);
    }
}

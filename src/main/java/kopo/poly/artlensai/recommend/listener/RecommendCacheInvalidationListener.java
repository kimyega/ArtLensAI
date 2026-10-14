package kopo.poly.artlensai.recommend.listener;

import kopo.poly.artlensai.recommend.event.TasteVectorInvalidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 취향 벡터가 바뀐 "후" 추천 캐시를 지운다.
 * @Transactional 메서드 안에서 즉시 delete하면 DB 커밋 전에 캐시가 비워져,
 * 그 틈에 들어온 추천 요청이 예전 취향벡터로 재계산해 다시 캐싱해버리는 레이스가 생긴다.
 * 그래서 AFTER_COMMIT 시점에만 캐시를 지운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendCacheInvalidationListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTasteVectorInvalidated(TasteVectorInvalidatedEvent event) {

        String cacheKey = "recommend:" + event.userId();

        redisTemplate.delete(cacheKey);

        log.info("추천 캐시 삭제: {}", cacheKey);
    }
}

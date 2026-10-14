package kopo.poly.artlensai.recommend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.artlensai.artist.domain.postgres.ArtistRepository;
import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistEntity;
import kopo.poly.artlensai.like.domain.postgres.ArtistLikeRepository;
import kopo.poly.artlensai.like.domain.postgres.entity.ArtistLikeEntity;
import kopo.poly.artlensai.recommend.domain.postgres.ArtistEmbeddingRepository;
import kopo.poly.artlensai.recommend.domain.postgres.SimilarArtistRow;
import kopo.poly.artlensai.recommend.domain.postgres.UserTasteEmbeddingRepository;
import kopo.poly.artlensai.recommend.domain.postgres.entity.ArtistEmbeddingEntity;
import kopo.poly.artlensai.recommend.domain.postgres.entity.UserTasteEmbeddingEntity;
import kopo.poly.artlensai.recommend.dto.RecommendResponseDTO;
import kopo.poly.artlensai.recommend.dto.RecommendSource;
import kopo.poly.artlensai.recommend.dto.RecommendedArtistDTO;
import kopo.poly.artlensai.recommend.event.TasteVectorInvalidatedEvent;
import kopo.poly.artlensai.recommend.service.IRecommendService;
import kopo.poly.artlensai.recommend.util.VectorMath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService implements IRecommendService {

    private static final int CANDIDATE_LIMIT = 30;
    private static final int POPULAR_LIMIT = 30;
    private static final long CACHE_TTL_HOURS = 6;

    private final ArtistLikeRepository artistLikeRepository;
    private final ArtistEmbeddingRepository artistEmbeddingRepository;
    private final UserTasteEmbeddingRepository userTasteEmbeddingRepository;
    private final ArtistRepository artistRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void recomputeTaste(Long userId) {

        log.info("{}.recomputeTaste Start! userId={}", getClass().getName(), userId);

        List<Long> likedArtistIds = findLikedArtistIds(userId);

        log.info("{}.recomputeTaste likedArtistCount={}", getClass().getName(), likedArtistIds.size());

        if (likedArtistIds.isEmpty()) {

            log.info("{}.recomputeTaste no liked artists, deleting taste vector", getClass().getName());

            userTasteEmbeddingRepository.deleteByUserId(userId);
            eventPublisher.publishEvent(new TasteVectorInvalidatedEvent(userId));

            log.info("{}.recomputeTaste End!", getClass().getName());
            return;
        }

        List<float[]> vectors = artistEmbeddingRepository.findByArtistIdIn(likedArtistIds)
                .stream()
                .map(ArtistEmbeddingEntity::getEmbdVect)
                .toList();

        if (vectors.isEmpty()) {

            log.info("{}.recomputeTaste no embeddings for liked artists, deleting taste vector", getClass().getName());

            // 찜한 작가들에 아직 임베딩이 하나도 없는 경우 (외부 파이프라인 지연 등)
            userTasteEmbeddingRepository.deleteByUserId(userId);
            eventPublisher.publishEvent(new TasteVectorInvalidatedEvent(userId));

            log.info("{}.recomputeTaste End!", getClass().getName());
            return;
        }

        float[] avgVector = VectorMath.average(vectors);

        UserTasteEmbeddingEntity entity = userTasteEmbeddingRepository.findByUserId(userId)
                .orElseGet(() -> UserTasteEmbeddingEntity.builder().userId(userId).build());

        entity.updateTaste(avgVector, likedArtistIds.size());

        userTasteEmbeddingRepository.save(entity);

        eventPublisher.publishEvent(new TasteVectorInvalidatedEvent(userId));

        log.info("{}.recomputeTaste End! likeCnt={}", getClass().getName(), likedArtistIds.size());
    }

    @Override
    public RecommendResponseDTO getRecommendations(Long userId) {

        log.info("{}.getRecommendations Start! userId={}", getClass().getName(), userId);

        String cacheKey = "recommend:" + userId;

        RecommendResponseDTO cached = readCache(cacheKey);

        if (cached != null) {

            log.info("{}.getRecommendations cache hit! cacheKey={}", getClass().getName(), cacheKey);
            log.info("{}.getRecommendations End! {}", getClass().getName(), cached);

            return cached;
        }

        log.info("{}.getRecommendations cache miss, cacheKey={}", getClass().getName(), cacheKey);

        Optional<UserTasteEmbeddingEntity> taste = userTasteEmbeddingRepository.findByUserId(userId);

        RecommendResponseDTO result = taste
                .map(t -> buildPersonalized(userId, t))
                .orElseGet(this::popularFallback);

        writeCache(cacheKey, result);

        log.info("{}.getRecommendations End! {}", getClass().getName(), result);

        return result;
    }

    private List<Long> findLikedArtistIds(Long userId) {

        return artistLikeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ArtistLikeEntity::getArtistId)
                .toList();
    }

    private RecommendResponseDTO buildPersonalized(Long userId, UserTasteEmbeddingEntity taste) {

        List<Long> likedArtistIds = findLikedArtistIds(userId);

        String tasteVecLiteral = VectorMath.toPgVectorLiteral(taste.getEmbdVect());

        List<SimilarArtistRow> candidates = artistEmbeddingRepository.findSimilarArtists(
                tasteVecLiteral, likedArtistIds, CANDIDATE_LIMIT
        );

        if (candidates.isEmpty()) {

            return popularFallback();
        }

        Map<Long, float[]> likedVectors = artistEmbeddingRepository.findByArtistIdIn(likedArtistIds)
                .stream()
                .collect(Collectors.toMap(ArtistEmbeddingEntity::getArtistId, ArtistEmbeddingEntity::getEmbdVect));

        List<Long> candidateIds = candidates.stream().map(SimilarArtistRow::getArtistId).toList();

        Map<Long, float[]> candidateVectors = artistEmbeddingRepository.findByArtistIdIn(candidateIds)
                .stream()
                .collect(Collectors.toMap(ArtistEmbeddingEntity::getArtistId, ArtistEmbeddingEntity::getEmbdVect));

        Map<Long, String> likedArtistNames = artistRepository.findAllById(likedVectors.keySet())
                .stream()
                .collect(Collectors.toMap(ArtistEntity::getArtistId, ArtistEntity::getName));

        List<RecommendedArtistDTO> items = candidates.stream()
                .map(row -> toRecommendedArtistDTO(row, candidateVectors.get(row.getArtistId()), likedVectors, likedArtistNames))
                .toList();

        return RecommendResponseDTO.builder()
                .source(RecommendSource.PERSONALIZED)
                .items(items)
                .build();
    }

    private RecommendedArtistDTO toRecommendedArtistDTO(
            SimilarArtistRow row,
            float[] candidateVector,
            Map<Long, float[]> likedVectors,
            Map<Long, String> likedArtistNames
    ) {

        Long reasonArtistId = null;
        double bestSimilarity = -1;

        if (candidateVector != null) {

            for (Map.Entry<Long, float[]> entry : likedVectors.entrySet()) {

                double similarity = VectorMath.cosineSimilarity(candidateVector, entry.getValue());

                if (similarity > bestSimilarity) {

                    bestSimilarity = similarity;
                    reasonArtistId = entry.getKey();
                }
            }
        }

        return RecommendedArtistDTO.builder()
                .artistId(row.getArtistId())
                .similarityPercent(clampPercent(row.getSimilarity()))
                .reasonArtistId(reasonArtistId)
                .reasonArtistName(reasonArtistId != null ? likedArtistNames.get(reasonArtistId) : null)
                .fallback(false)
                .build();
    }

    private RecommendResponseDTO popularFallback() {

        List<RecommendedArtistDTO> items = artistRepository.findTop30ByOrderByPostCntDesc()
                .stream()
                .limit(POPULAR_LIMIT)
                .map(a -> RecommendedArtistDTO.builder()
                        .artistId(a.getArtistId())
                        .postCnt(a.getPostCnt())
                        .fallback(true)
                        .build())
                .toList();

        return RecommendResponseDTO.builder()
                .source(RecommendSource.POPULAR_FALLBACK)
                .items(items)
                .build();
    }

    private int clampPercent(double similarity) {

        long percent = Math.round(similarity * 100);

        return (int) Math.max(0, Math.min(100, percent));
    }

    private RecommendResponseDTO readCache(String cacheKey) {

        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (!(cached instanceof String json)) {

            return null;
        }

        try {

            return objectMapper.readValue(json, RecommendResponseDTO.class);

        } catch (Exception e) {

            log.warn("추천 캐시 역직렬화 실패, 캐시 미스로 처리: {}", cacheKey, e);
            return null;
        }
    }

    private void writeCache(String cacheKey, RecommendResponseDTO result) {

        try {

            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_HOURS, TimeUnit.HOURS);

        } catch (Exception e) {

            log.warn("추천 캐시 저장 실패: {}", cacheKey, e);
        }
    }
}

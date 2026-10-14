package kopo.poly.artlensai.share.service.impl;

import kopo.poly.artlensai.analysis.repository.mongo.AnalysisResultsRepository;
import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import kopo.poly.artlensai.analysis.service.impl.AnalysisService;
import kopo.poly.artlensai.share.domain.postgres.ShareLinkRepository;
import kopo.poly.artlensai.share.domain.postgres.entity.ShareLinkEntity;
import kopo.poly.artlensai.share.dto.ShareLinkDTO;
import kopo.poly.artlensai.share.dto.SharedAnalysisResultDTO;
import kopo.poly.artlensai.share.service.IShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService implements IShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final AnalysisResultsRepository analysisResultsRepository;
    private final AnalysisService analysisService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${share.default-expire-days:30}")
    private int defaultExpireDays;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public ShareLinkDTO createOrGetShare(Long userId, String resultId, Integer expiresInDays) {

        log.info("{}.createOrGetShare Start! userId={}, resultId={}, expiresInDays={}",
                getClass().getName(), userId, resultId, expiresInDays);

        AnalysisResultsDocument doc =
                analysisResultsRepository.findById(resultId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "공유 링크를 찾을 수 없습니다."));

        if (!doc.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인의 분석 결과만 공유할 수 있습니다.");
        }

        Optional<ShareLinkEntity> existing =
                shareLinkRepository.findByResultIdAndUserIdAndIsPublicTrue(resultId, userId);

        if (existing.isPresent()) {

            ShareLinkEntity entity = existing.get();

            if (!isExpired(entity)) {

                ShareLinkDTO result = ShareLinkDTO.from(entity, frontendUrl, resolveViewCount(entity));

                log.info("{}.createOrGetShare End! (existing) {}", getClass().getName(), result);

                return result;
            }

            entity.revoke();
            shareLinkRepository.save(entity);
        }

        int days = expiresInDays != null ? expiresInDays : defaultExpireDays;

        ShareLinkEntity entity =
                ShareLinkEntity.builder()
                        .token(UUID.randomUUID().toString())
                        .resultId(resultId)
                        .userId(userId)
                        .isPublic(true)
                        .expiresAt(LocalDateTime.now().plusDays(days))
                        .viewCount(0L)
                        .createdAt(LocalDateTime.now())
                        .build();

        ShareLinkEntity saved = shareLinkRepository.save(entity);

        ShareLinkDTO result = ShareLinkDTO.from(saved, frontendUrl, resolveViewCount(saved));

        log.info("{}.createOrGetShare End! (created) {}", getClass().getName(), result);

        return result;
    }

    @Override
    public Optional<ShareLinkDTO> getMyShare(Long userId, String resultId) {

        log.info("{}.getMyShare Start! userId={}, resultId={}", getClass().getName(), userId, resultId);

        Optional<ShareLinkDTO> result = shareLinkRepository.findByResultIdAndUserIdAndIsPublicTrue(resultId, userId)
                .filter(entity -> !isExpired(entity))
                .map(entity -> ShareLinkDTO.from(entity, frontendUrl, resolveViewCount(entity)));

        log.info("{}.getMyShare End! present={}", getClass().getName(), result.isPresent());

        return result;
    }

    @Override
    public Map<String, ShareLinkDTO> getMyShares(Long userId, List<String> resultIds) {

        log.info("{}.getMyShares Start! userId={}, resultIdCount={}",
                getClass().getName(), userId, resultIds == null ? 0 : resultIds.size());

        if (resultIds == null || resultIds.isEmpty()) {

            log.info("{}.getMyShares End! (empty request)", getClass().getName());

            return Map.of();
        }

        Map<String, ShareLinkDTO> result = shareLinkRepository.findByResultIdInAndUserIdAndIsPublicTrue(resultIds, userId)
                .stream()
                .filter(entity -> !isExpired(entity))
                .collect(Collectors.toMap(
                        ShareLinkEntity::getResultId,
                        entity -> ShareLinkDTO.from(entity, frontendUrl, resolveViewCount(entity))
                ));

        log.info("{}.getMyShares End! resultCount={}", getClass().getName(), result.size());

        return result;
    }

    @Override
    public void revoke(Long userId, Long shareId) {

        log.info("{}.revoke Start! userId={}, shareId={}", getClass().getName(), userId, shareId);

        ShareLinkEntity entity =
                shareLinkRepository.findByShareIdAndUserId(shareId, userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "공유 링크를 찾을 수 없습니다."));

        entity.revoke();

        shareLinkRepository.save(entity);

        log.info("{}.revoke End!", getClass().getName());
    }

    @Override
    public SharedAnalysisResultDTO getPublicResult(String token) {

        log.info("{}.getPublicResult Start! token={}", getClass().getName(), token);

        ShareLinkEntity entity =
                shareLinkRepository.findByToken(token)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "공유 링크를 찾을 수 없습니다."));

        if (!Boolean.TRUE.equals(entity.getIsPublic()) || isExpired(entity)) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공유 링크를 찾을 수 없습니다.");
        }

        redisTemplate.opsForValue().increment("share-view:" + token);

        AnalysisResultsDocument doc =
                analysisService.getAnalysisResult(entity.getResultId());

        SharedAnalysisResultDTO result = SharedAnalysisResultDTO.from(doc);

        log.info("{}.getPublicResult End! resultId={}", getClass().getName(), entity.getResultId());

        return result;
    }

    private boolean isExpired(ShareLinkEntity entity) {

        return entity.getExpiresAt() != null
                && entity.getExpiresAt().isBefore(LocalDateTime.now());
    }

    /**
     * DB에 반영된 viewCount + Redis에 아직 배치(ShareViewFlushJob)로 반영되지 않은 증가분을 합쳐서,
     * 5분 배치 지연 없이 항상 최신 조회수를 보여준다.
     */
    private long resolveViewCount(ShareLinkEntity entity) {

        Object pending = redisTemplate.opsForValue().get("share-view:" + entity.getToken());

        long pendingCount = pending != null ? Long.parseLong(pending.toString()) : 0L;

        return entity.getViewCount() + pendingCount;
    }
}

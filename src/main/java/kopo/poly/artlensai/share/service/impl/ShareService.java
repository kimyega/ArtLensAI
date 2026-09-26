package kopo.poly.artlensai.share.service.impl;

import kopo.poly.artlensai.analysis.repository.mongo.AnalysisResultsRepository;
import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import kopo.poly.artlensai.analysis.service.impl.AnalysisService;
import kopo.poly.artlensai.share.domain.postgres.ShareLinkRepository;
import kopo.poly.artlensai.share.domain.postgres.entity.ShareLinkEntity;
import kopo.poly.artlensai.share.dto.ShareLinkDTO;
import kopo.poly.artlensai.share.dto.SharedAnalysisResultDTO;
import kopo.poly.artlensai.share.exception.ShareAccessDeniedException;
import kopo.poly.artlensai.share.exception.ShareNotFoundException;
import kopo.poly.artlensai.share.service.IShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        AnalysisResultsDocument doc =
                analysisResultsRepository.findById(resultId)
                        .orElseThrow(ShareNotFoundException::new);

        if (!doc.getUserId().equals(userId)) {

            throw new ShareAccessDeniedException();
        }

        Optional<ShareLinkEntity> existing =
                shareLinkRepository.findByResultIdAndUserIdAndIsPublicTrue(resultId, userId);

        if (existing.isPresent()) {

            ShareLinkEntity entity = existing.get();

            if (!isExpired(entity)) {

                return toDTO(entity);
            }

            entity.setIsPublic(false);
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

        return toDTO(saved);
    }

    @Override
    public Optional<ShareLinkDTO> getMyShare(Long userId, String resultId) {

        return shareLinkRepository.findByResultIdAndUserIdAndIsPublicTrue(resultId, userId)
                .filter(entity -> !isExpired(entity))
                .map(this::toDTO);
    }

    @Override
    public Map<String, ShareLinkDTO> getMyShares(Long userId, List<String> resultIds) {

        if (resultIds == null || resultIds.isEmpty()) {

            return Map.of();
        }

        return shareLinkRepository.findByResultIdInAndUserIdAndIsPublicTrue(resultIds, userId)
                .stream()
                .filter(entity -> !isExpired(entity))
                .collect(Collectors.toMap(ShareLinkEntity::getResultId, this::toDTO));
    }

    @Override
    public void revoke(Long userId, Long shareId) {

        ShareLinkEntity entity =
                shareLinkRepository.findByShareIdAndUserId(shareId, userId)
                        .orElseThrow(ShareNotFoundException::new);

        entity.setIsPublic(false);

        shareLinkRepository.save(entity);
    }

    @Override
    public SharedAnalysisResultDTO getPublicResult(String token) {

        ShareLinkEntity entity =
                shareLinkRepository.findByToken(token)
                        .orElseThrow(ShareNotFoundException::new);

        if (!Boolean.TRUE.equals(entity.getIsPublic()) || isExpired(entity)) {

            throw new ShareNotFoundException();
        }

        redisTemplate.opsForValue().increment("share-view:" + token);

        AnalysisResultsDocument doc =
                analysisService.getAnalysisResult(entity.getResultId());

        return SharedAnalysisResultDTO.builder()
                .imgKey(doc.getImgKey())
                .title(doc.getTitle())
                .comment(doc.getComment())
                .tags(doc.getTags())
                .palette(doc.getPalette())
                .top5(doc.getTop5())
                .styleMap(doc.getStyleMap())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private boolean isExpired(ShareLinkEntity entity) {

        return entity.getExpiresAt() != null
                && entity.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private ShareLinkDTO toDTO(ShareLinkEntity entity) {

        return ShareLinkDTO.builder()
                .shareId(entity.getShareId())
                .token(entity.getToken())
                .shareUrl(frontendUrl + "/share/" + entity.getToken())
                .isPublic(entity.getIsPublic())
                .expiresAt(entity.getExpiresAt())
                .viewCount(entity.getViewCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

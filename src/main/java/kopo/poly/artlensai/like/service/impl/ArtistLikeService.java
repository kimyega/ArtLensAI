package kopo.poly.artlensai.like.service.impl;

import kopo.poly.artlensai.like.domain.postgres.ArtistLikeRepository;
import kopo.poly.artlensai.like.domain.postgres.entity.ArtistLikeEntity;
import kopo.poly.artlensai.like.dto.LikeStatusDTO;
import kopo.poly.artlensai.like.dto.LikeToggleResponseDTO;
import kopo.poly.artlensai.like.service.IArtistLikeService;
import kopo.poly.artlensai.recommend.service.IRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistLikeService implements IArtistLikeService {

    private final ArtistLikeRepository artistLikeRepository;

    private final IRecommendService recommendService;

    @Override
    public LikeStatusDTO getLikedStatus(Long userId) {

        log.info("{}.getLikedStatus Start! userId={}", getClass().getName(), userId);

        var likes = artistLikeRepository.findByUserIdOrderByCreatedAtDesc(userId);

        LikeStatusDTO result = LikeStatusDTO.builder()
                .artistIds(likes.stream().map(ArtistLikeEntity::getArtistId).toList())
                .likeCount(likes.size())
                .build();

        log.info("{}.getLikedStatus End! {}", getClass().getName(), result);

        return result;
    }

    @Override
    @Transactional
    public LikeToggleResponseDTO like(Long userId, Long artistId) {

        log.info("{}.like Start! userId={}, artistId={}", getClass().getName(), userId, artistId);

        boolean alreadyLiked = artistLikeRepository.findByUserIdAndArtistId(userId, artistId).isPresent();

        log.info("{}.like alreadyLiked={}", getClass().getName(), alreadyLiked);

        if (!alreadyLiked) {

            artistLikeRepository.save(
                    ArtistLikeEntity.builder()
                            .userId(userId)
                            .artistId(artistId)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            recommendService.recomputeTaste(userId);
        }

        LikeToggleResponseDTO result = toResponse(userId, artistId, true);

        log.info("{}.like End! {}", getClass().getName(), result);

        return result;
    }

    @Override
    @Transactional
    public LikeToggleResponseDTO unlike(Long userId, Long artistId) {

        log.info("{}.unlike Start! userId={}, artistId={}", getClass().getName(), userId, artistId);

        boolean wasLiked = artistLikeRepository.findByUserIdAndArtistId(userId, artistId).isPresent();

        log.info("{}.unlike wasLiked={}", getClass().getName(), wasLiked);

        if (wasLiked) {

            artistLikeRepository.deleteByUserIdAndArtistId(userId, artistId);

            recommendService.recomputeTaste(userId);
        }

        LikeToggleResponseDTO result = toResponse(userId, artistId, false);

        log.info("{}.unlike End! {}", getClass().getName(), result);

        return result;
    }

    private LikeToggleResponseDTO toResponse(Long userId, Long artistId, boolean liked) {

        long likeCount = artistLikeRepository.countByUserId(userId);

        return LikeToggleResponseDTO.builder()
                .artistId(artistId)
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}

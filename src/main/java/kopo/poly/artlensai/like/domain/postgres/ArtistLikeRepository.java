package kopo.poly.artlensai.like.domain.postgres;

import kopo.poly.artlensai.like.domain.postgres.entity.ArtistLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistLikeRepository
        extends JpaRepository<ArtistLikeEntity, Long> {

    Optional<ArtistLikeEntity> findByUserIdAndArtistId(Long userId, Long artistId);

    List<ArtistLikeEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    void deleteByUserIdAndArtistId(Long userId, Long artistId);
}

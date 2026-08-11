package kopo.poly.artlensai.artist.domain.postgres;

import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistImageRepository
        extends JpaRepository<ArtistImageEntity, Long> {

    // =========================
    // 작가별 이미지 조회
    // =========================
    List<ArtistImageEntity>
    findByArtistId(
            Long artistId
    );
}
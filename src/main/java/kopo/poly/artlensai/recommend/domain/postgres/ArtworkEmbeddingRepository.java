package kopo.poly.artlensai.recommend.domain.postgres;

import kopo.poly.artlensai.recommend.domain.postgres.entity.ArtworkEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArtworkEmbeddingRepository
        extends JpaRepository<ArtworkEmbeddingEntity, Long> {

    List<ArtworkEmbeddingEntity> findByImgIdIn(Collection<Long> imgIds);

    /**
     * 클러스터별로 랜덤 이미지 1장씩 조회한다 (취향 테스트 초반 explore 문제용).
     * excludeArtistIds가 비어있으면(IN () 문법 에러 방지) 내부적으로 더미 값을 채워서
     * 실제 쿼리를 호출하므로, 호출 측은 빈 리스트를 그대로 넘겨도 된다.
     */
    default List<ArtworkCandidateRow> findRandomPerCluster(List<Long> excludeArtistIds) {

        return findRandomPerClusterInternal(safeExcludeIds(excludeArtistIds));
    }

    /**
     * 주어진 벡터(vec)와 가장 가까운 이미지를 작가당 1장씩 조회한다
     * (취향 테스트 후반 exploit 문제용). excludeArtistIds 더미값 규칙은 위와 동일.
     */
    default List<ArtworkCandidateRow> findNearestDistinctArtistArtworks(
            String vec,
            List<Long> excludeArtistIds,
            int limit
    ) {

        return findNearestDistinctArtistArtworksInternal(vec, safeExcludeIds(excludeArtistIds), limit);
    }

    private static List<Long> safeExcludeIds(List<Long> excludeArtistIds) {

        return Optional.ofNullable(excludeArtistIds)
                .filter(ids -> !ids.isEmpty())
                .orElseGet(() -> List.of(-1L));
    }

    @Query(value = """
            SELECT DISTINCT ON (ac.cluster_label)
                   aw.img_id AS imgId,
                   aw.artist_id AS artistId,
                   ai.img_url AS imgUrl
            FROM artwork_embeddings aw
            JOIN artist_coords ac ON ac.artist_id = aw.artist_id
            JOIN artist_images ai ON ai.img_id = aw.img_id
            WHERE aw.artist_id NOT IN (:excludeArtistIds)
            ORDER BY ac.cluster_label, random()
            """, nativeQuery = true)
    List<ArtworkCandidateRow> findRandomPerClusterInternal(
            @Param("excludeArtistIds") List<Long> excludeArtistIds
    );

    @Query(value = """
            SELECT sub.img_id AS imgId, sub.artist_id AS artistId, sub.img_url AS imgUrl
            FROM (
                SELECT DISTINCT ON (aw.artist_id)
                       aw.img_id AS img_id,
                       aw.artist_id AS artist_id,
                       ai.img_url AS img_url,
                       aw.embd_vect <=> CAST(:vec AS vector) AS distance
                FROM artwork_embeddings aw
                JOIN artist_images ai ON ai.img_id = aw.img_id
                WHERE aw.artist_id NOT IN (:excludeArtistIds)
                ORDER BY aw.artist_id, aw.embd_vect <=> CAST(:vec AS vector)
            ) sub
            ORDER BY sub.distance
            LIMIT :limit
            """, nativeQuery = true)
    List<ArtworkCandidateRow> findNearestDistinctArtistArtworksInternal(
            @Param("vec") String vec,
            @Param("excludeArtistIds") List<Long> excludeArtistIds,
            @Param("limit") int limit
    );
}

package kopo.poly.artlensai.recommend.domain.postgres;

import kopo.poly.artlensai.recommend.domain.postgres.entity.ArtistEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArtistEmbeddingRepository
        extends JpaRepository<ArtistEmbeddingEntity, Long> {

    List<ArtistEmbeddingEntity> findByArtistIdIn(Collection<Long> artistIds);

    /**
     * 취향 벡터(tasteVec, "[0.1,0.2,...]" 형태의 pgvector literal 문자열)와 코사인 유사도가
     * 가까운 작가를 조회한다. excludeIds가 비어있으면(IN () 문법 에러 방지) 내부적으로
     * 더미 값을 채워서 실제 쿼리를 호출하므로, 호출 측은 빈 리스트를 그대로 넘겨도 된다.
     */
    default List<SimilarArtistRow> findSimilarArtists(String tasteVec, List<Long> excludeIds, int limit) {

        List<Long> safeExcludeIds = Optional.ofNullable(excludeIds)
                .filter(ids -> !ids.isEmpty())
                .orElseGet(() -> List.of(-1L));

        return findSimilarArtistsInternal(tasteVec, safeExcludeIds, limit);
    }

    @Query(value = """
            SELECT ae.artist_id AS artistId,
                   1 - (ae.embd_vect <=> CAST(:tasteVec AS vector)) AS similarity
            FROM artist_embeddings ae
            WHERE ae.artist_id NOT IN (:excludeIds)
            ORDER BY ae.embd_vect <=> CAST(:tasteVec AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<SimilarArtistRow> findSimilarArtistsInternal(
            @Param("tasteVec") String tasteVec,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("limit") int limit
    );
}

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

    Optional<ArtistEmbeddingEntity> findByArtistId(Long artistId);

    List<ArtistEmbeddingEntity> findByArtistIdIn(Collection<Long> artistIds);

    /**
     * 취향 벡터(tasteVec, "[0.1,0.2,...]" 형태의 pgvector literal 문자열)와 코사인 유사도가
     * 가까운 작가를 조회한다. excludeIds는 비어있으면 안 됨(IN () 문법 에러) — 호출 측에서
     * 제외할 작가가 없으면 더미 값(예: -1)을 하나 넣어서 호출해야 한다.
     */
    @Query(value = """
            SELECT ae.artist_id AS artistId,
                   1 - (ae.embd_vect <=> CAST(:tasteVec AS vector)) AS similarity
            FROM artist_embeddings ae
            WHERE ae.artist_id NOT IN (:excludeIds)
            ORDER BY ae.embd_vect <=> CAST(:tasteVec AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<SimilarArtistRow> findSimilarArtists(
            @Param("tasteVec") String tasteVec,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("limit") int limit
    );
}

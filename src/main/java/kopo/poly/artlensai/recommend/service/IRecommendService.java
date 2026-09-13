package kopo.poly.artlensai.recommend.service;

import kopo.poly.artlensai.recommend.dto.RecommendResponseDTO;

public interface IRecommendService {

    /**
     * 유저가 찜한 작가들의 임베딩 벡터 평균으로 취향 벡터를 재계산해서 저장한다.
     * 찜한 작가가 없거나 임베딩이 하나도 없으면 취향 벡터를 삭제한다.
     * 끝에 추천 캐시 무효화 이벤트를 발행한다(실제 삭제는 트랜잭션 커밋 후).
     */
    void recomputeTaste(Long userId);

    /**
     * 추천 작가 목록을 조회한다. Redis 캐시 우선, 없으면 pgvector 코사인 검색 후 캐싱.
     * 취향 벡터가 없으면 인기 작가 fallback을 반환한다.
     */
    RecommendResponseDTO getRecommendations(Long userId);
}

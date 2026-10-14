package kopo.poly.artlensai.tastetest.service.impl;

import kopo.poly.artlensai.artist.domain.postgres.ArtistRepository;
import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistEntity;
import kopo.poly.artlensai.artist.domain.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.recommend.domain.postgres.ArtistEmbeddingRepository;
import kopo.poly.artlensai.recommend.domain.postgres.ArtworkCandidateRow;
import kopo.poly.artlensai.recommend.domain.postgres.ArtworkEmbeddingRepository;
import kopo.poly.artlensai.recommend.domain.postgres.SimilarArtistRow;
import kopo.poly.artlensai.recommend.domain.postgres.entity.ArtworkEmbeddingEntity;
import kopo.poly.artlensai.recommend.util.VectorMath;
import kopo.poly.artlensai.stylemap.domain.postgres.entity.ArtistCoordEntity;
import kopo.poly.artlensai.stylemap.repository.mongo.ArtistCoordRepository;
import kopo.poly.artlensai.tastetest.domain.postgres.TasteTestAnswerRepository;
import kopo.poly.artlensai.tastetest.domain.postgres.TasteTestRepository;
import kopo.poly.artlensai.tastetest.domain.postgres.entity.TasteTestAnswerEntity;
import kopo.poly.artlensai.tastetest.domain.postgres.entity.TasteTestEntity;
import kopo.poly.artlensai.tastetest.dto.TasteAnswerRequestDTO;
import kopo.poly.artlensai.tastetest.dto.TasteQuestionOptionDTO;
import kopo.poly.artlensai.tastetest.dto.TasteResultArtistDTO;
import kopo.poly.artlensai.tastetest.dto.TasteTestResponseDTO;
import kopo.poly.artlensai.tastetest.service.ITasteTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasteTestService implements ITasteTestService {

    private static final int TOTAL_QUESTIONS = 10;
    private static final int OPTIONS_PER_QUESTION = 3;
    private static final int EXPLORE_QUESTIONS = 4;

    private final TasteTestRepository tasteTestRepository;
    private final TasteTestAnswerRepository tasteTestAnswerRepository;
    private final ArtworkEmbeddingRepository artworkEmbeddingRepository;
    private final ArtistEmbeddingRepository artistEmbeddingRepository;
    private final ArtistCoordRepository artistCoordRepository;
    private final ArtistRepository artistRepository;
    private final ArtistStyleProfileRepository artistStyleProfileRepository;

    @Override
    @Transactional
    public TasteTestResponseDTO startTest(Long userId) {

        log.info("{}.startTest Start! userId={}", getClass().getName(), userId);

        TasteTestEntity test = tasteTestRepository.save(
                TasteTestEntity.builder()
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        List<TasteQuestionOptionDTO> options = generateQuestion(test.getTestId(), 1, List.of());

        TasteTestResponseDTO result = TasteTestResponseDTO.builder()
                .testId(test.getTestId())
                .finished(false)
                .questionNo(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .options(options)
                .build();

        log.info("{}.startTest End! testId={}", getClass().getName(), test.getTestId());

        return result;
    }

    @Override
    @Transactional
    public TasteTestResponseDTO submitAnswer(Long testId, Long userId, TasteAnswerRequestDTO reqDTO) {

        log.info("{}.submitAnswer Start! testId={}, userId={}, pickedImgId={}",
                getClass().getName(), testId, userId, reqDTO.pickedImgId());

        TasteTestEntity test = tasteTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 취향 테스트입니다."));

        if (!test.getUserId().equals(userId)) {

            throw new RuntimeException("본인의 취향 테스트가 아닙니다.");
        }

        int nextQuestionNo = tasteTestAnswerRepository.countAnsweredQuestions(testId) + 1;

        for (Long imgId : reqDTO.shownImgIds()) {

            tasteTestAnswerRepository.save(
                    TasteTestAnswerEntity.builder()
                            .testId(testId)
                            .questionNo(nextQuestionNo)
                            .imgId(imgId)
                            .isPicked(imgId.equals(reqDTO.pickedImgId()))
                            .build()
            );
        }

        if (nextQuestionNo >= TOTAL_QUESTIONS) {

            TasteTestResponseDTO result = computeResult(test);

            log.info("{}.submitAnswer End! (finished) testId={}", getClass().getName(), testId);

            return result;
        }

        List<Long> excludeArtistIds = shownArtistIds(testId);

        int followingQuestionNo = nextQuestionNo + 1;

        List<TasteQuestionOptionDTO> options = generateQuestion(testId, followingQuestionNo, excludeArtistIds);

        TasteTestResponseDTO result = TasteTestResponseDTO.builder()
                .testId(testId)
                .finished(false)
                .questionNo(followingQuestionNo)
                .totalQuestions(TOTAL_QUESTIONS)
                .options(options)
                .build();

        log.info("{}.submitAnswer End! testId={}, nextQuestionNo={}", getClass().getName(), testId, followingQuestionNo);

        return result;
    }

    private List<TasteQuestionOptionDTO> generateQuestion(Long testId, int questionNo, List<Long> excludeArtistIds) {

        List<ArtworkCandidateRow> candidates;

        if (questionNo <= EXPLORE_QUESTIONS) {

            candidates = new ArrayList<>(artworkEmbeddingRepository.findRandomPerCluster(excludeArtistIds));
            Collections.shuffle(candidates);

        } else {

            List<Long> pickedImgIds = tasteTestAnswerRepository.findDistinctPickedImgIds(testId);

            float[] avgVec = averageEmbeddingOrThrow(pickedImgIds);

            candidates = artworkEmbeddingRepository.findNearestDistinctArtistArtworks(
                    VectorMath.toPgVectorLiteral(avgVec),
                    excludeArtistIds,
                    OPTIONS_PER_QUESTION
            );
        }

        return candidates.stream()
                .limit(OPTIONS_PER_QUESTION)
                .map(row -> TasteQuestionOptionDTO.builder()
                        .imgId(row.getImgId())
                        .artistId(row.getArtistId())
                        .imgUrl(row.getImgUrl())
                        .build())
                .toList();
    }

    private List<Long> shownArtistIds(Long testId) {

        List<Long> shownImgIds = tasteTestAnswerRepository.findByTestId(testId)
                .stream()
                .map(TasteTestAnswerEntity::getImgId)
                .toList();

        if (shownImgIds.isEmpty()) {

            return List.of();
        }

        return artworkEmbeddingRepository.findByImgIdIn(shownImgIds)
                .stream()
                .map(ArtworkEmbeddingEntity::getArtistId)
                .distinct()
                .toList();
    }

    private TasteTestResponseDTO computeResult(TasteTestEntity test) {

        List<Long> pickedImgIds = tasteTestAnswerRepository.findDistinctPickedImgIds(test.getTestId());

        float[] tasteVect = averageEmbeddingOrThrow(pickedImgIds);

        List<SimilarArtistRow> nearestArtists = artistEmbeddingRepository.findSimilarArtists(
                VectorMath.toPgVectorLiteral(tasteVect), List.of(-1L), 5
        );

        Map<Long, String> artistNames = artistRepository.findAllById(
                        nearestArtists.stream().map(SimilarArtistRow::getArtistId).toList()
                )
                .stream()
                .collect(Collectors.toMap(ArtistEntity::getArtistId, ArtistEntity::getName));

        List<TasteResultArtistDTO> topArtists = nearestArtists.stream()
                .map(row -> TasteResultArtistDTO.builder()
                        .artistId(row.getArtistId())
                        .artistName(artistNames.get(row.getArtistId()))
                        .profileImg(artistStyleProfileRepository.findByArtistId(row.getArtistId().intValue())
                                .map(doc -> doc.profileImg())
                                .orElse(null))
                        .similarityPercent(clampPercent(row.getSimilarity()))
                        .build())
                .toList();

        String clusterLabel = nearestArtists.stream()
                .findFirst()
                .map(SimilarArtistRow::getArtistId)
                .flatMap(artistCoordRepository::findByArtistId)
                .map(ArtistCoordEntity::getClusterLabel)
                .orElse(null);

        test.applyResult(tasteVect, clusterLabel);
        tasteTestRepository.save(test);

        return TasteTestResponseDTO.builder()
                .testId(test.getTestId())
                .finished(true)
                .clusterLabel(clusterLabel)
                .topArtists(topArtists)
                .build();
    }

    /**
     * 선택한 이미지들의 임베딩 평균 벡터를 계산한다.
     * 아직 임베딩이 하나도 안 만들어진 콜드스타트 상황이면, 내부 예외 메시지를 그대로
     * 노출하는 대신 사용자에게 보여줄 수 있는 메시지로 바꿔서 던진다.
     */
    private float[] averageEmbeddingOrThrow(List<Long> imgIds) {

        List<float[]> vectors = artworkEmbeddingRepository.findByImgIdIn(imgIds)
                .stream()
                .map(ArtworkEmbeddingEntity::getEmbdVect)
                .toList();

        if (vectors.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "아직 선택한 이미지들의 분석 데이터가 준비되지 않았어요. 잠시 후 다시 시도해주세요."
            );
        }

        return VectorMath.average(vectors);
    }

    private int clampPercent(double similarity) {

        long percent = Math.round(similarity * 100);

        return (int) Math.max(0, Math.min(100, percent));
    }
}

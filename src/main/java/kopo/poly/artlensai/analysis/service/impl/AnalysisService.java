package kopo.poly.artlensai.analysis.service.impl;

import kopo.poly.artlensai.analysis.domain.postgres.entity.AnalysisHistoryEntity;
import kopo.poly.artlensai.analysis.domain.postgres.entity.UploadImageEntity;
import kopo.poly.artlensai.analysis.dto.AnalysisResultDTO;
import kopo.poly.artlensai.analysis.dto.AnalysisStartResponseDTO;
import kopo.poly.artlensai.analysis.dto.TagResultDTO;
import kopo.poly.artlensai.analysis.repository.mongo.AnalysisResultsRepository;
import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import kopo.poly.artlensai.analysis.repository.postgres.AnalysisHistoryRepository;
import kopo.poly.artlensai.analysis.repository.postgres.UploadImageRepository;
import kopo.poly.artlensai.analysis.service.IAnalysisService;
import kopo.poly.artlensai.artist.repository.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService implements IAnalysisService {

    private final UploadImageRepository uploadImageRepository;
    private final AnalysisHistoryRepository historyRepository;
    private final MongoService mongoService;
    private final AnalysisResultsRepository analysisResultsRepository;
    private final ArtistStyleProfileRepository artistStyleProfileRepository;
    private final S3Client s3Client;

    @Value("${ncp.bucket}")
    private String bucket;

    @Override
    public Long saveUploadImage(
            MultipartFile file,
            UserDTO pDTO
    ) {

        try {

            String fileName =
                    UUID.randomUUID() + "_"
                            + file.getOriginalFilename();

            String objectKey =
                    "uploads/" + fileName;

            // 가로 세로 추출
            BufferedImage image =
                    ImageIO.read(file.getInputStream());

            Integer width = null;
            Integer height = null;

            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
            }

            // Object Storage 업로드
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );

            UploadImageEntity entity =
                    UploadImageEntity.builder()
                            .userId(pDTO.userId())
                            .imgKey(objectKey)
                            .fileName(file.getOriginalFilename())
                            .fileSize(file.getSize())
                            .width(width)
                            .height(height)
                            .format(file.getContentType())
                            .build();

            UploadImageEntity saved =
                    uploadImageRepository.save(entity);

            return saved.getUploadId();

        } catch (Exception e) {

            log.error("upload fail", e);

            throw new RuntimeException(
                    "파일 저장 실패",
                    e
            );
        }
    }

    @Override
    public AnalysisStartResponseDTO startAnalysis(
            Long uploadId,
            UserDTO userDTO
    ) {

        UploadImageEntity image =
                uploadImageRepository.findById(uploadId)
                        .orElseThrow(
                                () -> new RuntimeException("이미지 없음")
                        );

        AnalysisHistoryEntity history =
                AnalysisHistoryEntity.builder()
                        .userId(userDTO.userId())
                        .uploadId(uploadId)
                        .status("PENDING")
                        .build();

        history = historyRepository.save(history);

        long startTime =
                System.currentTimeMillis();

        try {

            log.info(
                    "🚀 START ANALYSIS uploadId={}",
                    uploadId
            );

            log.info(
                    "📦 imgKey={}",
                    image.getImgKey()
            );

            WebClient webClient =
                    WebClient.builder()
                            .baseUrl("http://localhost:8000")
                            .build();

            Map<String, Object> req =
                    new HashMap<>();

            req.put(
                    "imgKey",
                    image.getImgKey()
            );

            // =============================
            // FastAPI 호출
            // =============================
            AnalysisResultDTO body =
                    webClient.post()
                            .uri("/analysis")
                            .bodyValue(req)
                            .retrieve()
                            .bodyToMono(
                                    AnalysisResultDTO.class
                            )
                            .block();

            log.info(
                    "🔥 STYLE_MAP BODY = {}",
                    body.styleMap()
            );

            var enrichedTop5 =

                    body.top5()
                            .stream()
                            .map(artist -> {

                                var profile =

                                        artistStyleProfileRepository
                                                .findByArtistId(
                                                        artist.artistId()
                                                                .intValue()
                                                )
                                                .orElse(null);

                                String profileImg = "";
                                String mainTag = "";

                                if (profile != null) {

                                    profileImg =
                                            profile.profileImg();

                                    if (
                                            profile.tags() != null
                                                    &&
                                                    !profile.tags().isEmpty()
                                    ) {

                                        mainTag =
                                                "#"
                                                        + profile.tags().get(0);
                                    }
                                }

                                return artist.toBuilder()

                                        .profileImg(profileImg)

                                        .mainTag(mainTag)

                                        .build();
                            })
                            .toList();

            body = body.toBuilder()

                    .top5(enrichedTop5)

                    .build();

            log.info(
                    "🔥 FastAPI RESPONSE = {}",
                    body
            );

            // =============================
            // NULL 체크
            // =============================
            if (body == null) {

                log.error("❌ body is NULL");

                failHistory(history);

                throw new RuntimeException(
                        "분석 결과 없음"
                );
            }

            log.info(
                    "🔥 RESULT = {}",
                    body.result()
            );

            // =============================
            // TOP5 로그
            // =============================
            if (body.top5() != null) {

                log.info(
                        "🔥 TOP5 ====================="
                );

                body.top5().forEach(a ->

                        log.info(
                                "artistId={}, artistName={}, sim={}",
                                a.artistId(),
                                a.artistName(),
                                a.similarity()
                        )
                );

            } else {

                log.warn("⚠ TOP5 is NULL");
            }

            // =============================
            // TAGS 로그
            // =============================
            if (body.tags() != null) {

                log.info(
                        "🔥 TAGS ====================="
                );

                body.tags().forEach(tag ->

                        log.info(
                                "tag={}, score={}",
                                tag.tag(),
                                tag.score()
                        )
                );

            } else {

                log.warn("⚠ TAGS is NULL");
            }

            // =============================
            // TITLE 로그
            // =============================
            log.info(
                    "🔥 TITLE = {}",
                    body.title()
            );

            // =============================
            // 성공 처리
            // =============================
            if (body.result() == 1) {

                // =============================
                // Mongo 저장
                // =============================
                String mongoId =

                        mongoService.saveAnalysisResult(
                                history,
                                image,
                                body
                        );

                log.info(
                        "🔥 mongoId={}",
                        mongoId
                );

                historyRepository.save(

                        AnalysisHistoryEntity.builder()

                                .historyId(
                                        history.getHistoryId()
                                )

                                .userId(
                                        history.getUserId()
                                )

                                .uploadId(
                                        history.getUploadId()
                                )

                                .status("DONE")

                                .processingTime(
                                        (
                                                System.currentTimeMillis()
                                                        - startTime
                                        ) / 1000f
                                )

                                .build()
                );

                log.info(
                        "✅ ANALYSIS DONE historyId={}",
                        history.getHistoryId()
                );

                // =============================
                // 🔥 React 로 반환
                // =============================
                return AnalysisStartResponseDTO
                        .builder()
                        .result(1)
                        .resultId(mongoId)
                        .build();

            } else {

                failHistory(history);

                log.error("❌ RESULT != 1");

                throw new RuntimeException(
                        "AI 분석 실패"
                );
            }

        } catch (Exception e) {

            log.error(
                    "❌ FastAPI 호출 실패",
                    e
            );

            failHistory(history);

            throw new RuntimeException(
                    "FastAPI 호출 실패",
                    e
            );
        }
    }

    private void failHistory(AnalysisHistoryEntity history) {
        historyRepository.save(
                AnalysisHistoryEntity.builder()
                        .historyId(history.getHistoryId())
                        .userId(history.getUserId())
                        .uploadId(history.getUploadId())
                        .status("ERROR")
                        .errorMessage("AI 분석 실패")
                        .build()
        );
    }


    public AnalysisResultsDocument getAnalysisResult(
            String id
    ) {

        log.info(
                "📥 getAnalysisResult id={}",
                id
        );

        AnalysisResultsDocument document =

                analysisResultsRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException("결과 없음")
                        );


        // =========================
        // TAG 퍼센트 계산
        // =========================
        double maxScore =

                document.getTags()
                        .stream()
                        .mapToDouble(t -> t.score())
                        .max()
                        .orElse(1.0);

        double minScore =

                document.getTags()
                        .stream()
                        .mapToDouble(t -> t.score())
                        .min()
                        .orElse(0.0);

        log.info(
                "📊 maxScore={}, minScore={}",
                maxScore,
                minScore
        );

        // =========================
        // displayPercent 생성
        // =========================
        var convertedTags =

                document.getTags()
                        .stream()
                        .map(t -> {

                            double normalized;

                            if (maxScore == minScore) {

                                normalized = 1.0;

                            } else {

                                normalized =
                                        t.score()
                                                / maxScore;
                            }

                            normalized =
                                    Math.pow(
                                            normalized,
                                            0.7
                                    );

                            int percent =

                                    (int) Math.round(
                                            normalized * 85
                                    );

                            log.info(
                                    "🏷 tag={}, rawScore={}, percent={}",
                                    t.tag(),
                                    t.score(),
                                    percent
                            );

                            return TagResultDTO.builder()
                                    .tag(t.tag())
                                    .category(t.category())
                                    .score(t.score())
                                    .displayPercent(percent)
                                    .build();
                        })
                        .toList();

        // =========================
        // document tags 교체
        // =========================
        document.setTags(convertedTags);

        log.info(
                "✅ TAG 변환 완료"
        );

        return document;
    }
}
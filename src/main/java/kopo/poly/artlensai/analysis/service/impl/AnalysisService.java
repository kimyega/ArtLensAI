package kopo.poly.artlensai.analysis.service.impl;

import kopo.poly.artlensai.analysis.domain.postgres.entity.AnalysisHistoryEntity;
import kopo.poly.artlensai.analysis.domain.postgres.entity.UploadImageEntity;
import kopo.poly.artlensai.analysis.dto.AnalysisResultDTO;
import kopo.poly.artlensai.analysis.repository.postgres.AnalysisHistoryRepository;
import kopo.poly.artlensai.analysis.repository.postgres.UploadImageRepository;
import kopo.poly.artlensai.analysis.service.IAnalysisService;
import kopo.poly.artlensai.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

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

    @Override
    public Long saveUploadImage(MultipartFile file, UserDTO pDTO) {

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadDir = Paths.get("C:", "Users", "kimye", "Desktop",
                    "개인프로젝트", "2차 평가", "upload_img");

            Files.createDirectories(uploadDir);

            Path targetPath = uploadDir.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Integer width = null;
            Integer height = null;

            try (InputStream imageStream = Files.newInputStream(targetPath)) {
                BufferedImage image = ImageIO.read(imageStream);

                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();
                }
            }

            UploadImageEntity entity = UploadImageEntity.builder()
                    .userId(pDTO.userId())
                    .imgKey(fileName)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .width(width)
                    .height(height)
                    .format(file.getContentType())
                    .build();

            UploadImageEntity saved = uploadImageRepository.save(entity);

            return saved.getUploadId();

        } catch (Exception e) {
            log.error("upload fail", e);
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public void startAnalysis(Long uploadId, UserDTO userDTO) {


        UploadImageEntity image = uploadImageRepository.findById(uploadId)
                .orElseThrow(() -> new RuntimeException("이미지 없음"));

        AnalysisHistoryEntity history = AnalysisHistoryEntity.builder()
                .userId(userDTO.userId())
                .uploadId(uploadId)
                .status("PENDING")
                .build();

        history = historyRepository.save(history);

        long startTime = System.currentTimeMillis();

        try {
            log.info("🚀 START ANALYSIS uploadId={}", uploadId);
            log.info("📦 imgKey={}", image.getImgKey());

            WebClient webClient = WebClient.builder()
                    .baseUrl("http://localhost:8000")
                    .build();

            Map<String, Object> req = new HashMap<>();
            req.put("imgKey", image.getImgKey());

            AnalysisResultDTO body = webClient.post()
                    .uri("/analysis")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(AnalysisResultDTO.class)
                    .block();

            log.info("🔥 FastAPI RESPONSE = {}", body);

            // -----------------------------
            // null 체크
            // -----------------------------
            if (body == null) {
                log.error("❌ body is NULL");
                failHistory(history);
                return;
            }

            log.info("🔥 RESULT = {}", body.result());

            // -----------------------------
            // TOP5 로그
            // -----------------------------
            if (body.top5() != null) {
                log.info("🔥 TOP5 =====================");

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

            // -----------------------------
            // TAGS 로그
            // -----------------------------
            if (body.tags() != null) {
                log.info("🔥 TAGS =====================");

                body.tags().forEach(tag ->
                        log.info("tag={}", tag)
                );
            } else {
                log.warn("⚠ TAGS is NULL");
            }

            // -----------------------------
            // 성공 처리
            // -----------------------------
            if (body.result() == 1) {

                historyRepository.save(
                        AnalysisHistoryEntity.builder()
                                .historyId(history.getHistoryId())
                                .userId(history.getUserId())
                                .uploadId(history.getUploadId())
                                .status("DONE")
                                .processingTime(
                                        (System.currentTimeMillis() - startTime) / 1000f
                                )
                                .build()
                );

                log.info("✅ ANALYSIS DONE historyId={}", history.getHistoryId());

            } else {
                failHistory(history);
                log.error("❌ RESULT != 1");
            }

        } catch (Exception e) {

            log.error("❌ FastAPI 호출 실패", e);

            failHistory(history);
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
}
package kopo.poly.artlensai.artist.service.impl;

import kopo.poly.artlensai.analysis.dto.ArtistStyleProfileDTO;
import kopo.poly.artlensai.artist.domain.postgres.ArtistImageRepository;
import kopo.poly.artlensai.artist.domain.postgres.ArtistRepository;
import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistEntity;
import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistImageEntity;
import kopo.poly.artlensai.artist.dto.ArtistProfileRequestDTO;
import kopo.poly.artlensai.artist.dto.ArtistProfileResponseDTO;
import kopo.poly.artlensai.artist.service.IArtistProfileBuildService;
import kopo.poly.artlensai.artist.service.IArtistProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistProfileBuildService
        implements IArtistProfileBuildService {

    private final ArtistRepository artistRepository;

    private final ArtistImageRepository artistImageRepository;

    private final IArtistProfileService artistProfileService;

    private final WebClient.Builder webClientBuilder;

    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Override
    public void buildProfiles() {

        // =========================
        // FastAPI WebClient
        // =========================
        WebClient webClient =

                webClientBuilder
                        .baseUrl(fastApiUrl)
                        .build();

        // =========================
        // 전체 작가 조회
        // =========================
        List<ArtistEntity> artists =

                artistRepository.findAll();

        log.info(
                "🎨 PROFILE BUILD START size={}",
                artists.size()
        );

        // =========================
        // 작가 반복
        // =========================
        for (ArtistEntity artist : artists) {

            try {

                log.info(
                        "🚀 BUILD START artist={}",
                        artist.getName()
                );

                // =========================
                // 작가 이미지 조회
                // =========================
                List<String> imageUrls =

                        artistImageRepository
                                .findByArtistId(
                                        artist.getArtistId()
                                )
                                .stream()
                                .map(
                                        ArtistImageEntity::getImgUrl
                                )
                                .toList();

                if (imageUrls.isEmpty()) {

                    log.warn(
                            "⚠ NO IMAGE artist={}",
                            artist.getName()
                    );

                    continue;
                }

                log.info(
                        "🖼 image count={}",
                        imageUrls.size()
                );

                // =========================
                // FastAPI 요청 DTO
                // =========================
                ArtistProfileRequestDTO reqDTO =

                        ArtistProfileRequestDTO.builder()
                                .artistId(
                                        artist.getArtistId()
                                                .intValue()
                                )
                                .artistName(
                                        artist.getName()
                                )
                                .imageUrls(
                                        imageUrls
                                )
                                .build();

                log.info(
                        "📦 REQUEST DTO = {}",
                        reqDTO
                );

                // =========================
                // FastAPI 호출
                // =========================
                ArtistProfileResponseDTO resDTO =

                        webClient.post()
                                .uri("/artist/profile/build")
                                .bodyValue(reqDTO)
                                .retrieve()
                                .bodyToMono(
                                        ArtistProfileResponseDTO.class
                                )
                                .block();

                // =========================
                // NULL 체크
                // =========================
                if (resDTO == null) {

                    log.warn(
                            "⚠ EMPTY RESPONSE artist={}",
                            artist.getName()
                    );

                    continue;
                }

                log.info(
                        "🔥 RESPONSE = {}",
                        resDTO
                );

                // =========================
                // Mongo 저장 DTO
                // =========================
                ArtistStyleProfileDTO saveDTO =

                        ArtistStyleProfileDTO.builder()
                                .artistId(
                                        artist.getArtistId()

                                )
                                .artistName(
                                        artist.getName()
                                )
                                .styleDesc(
                                        resDTO.styleDesc()
                                )
                                .colors(
                                        resDTO.colors()
                                )
                                .tags(
                                        resDTO.tags()
                                )
                                .profileImg(
                                        resDTO.profileImg()
                                )
                                .build();

                log.info(
                        "💾 SAVE DTO = {}",
                        saveDTO
                );

                // =========================
                // Mongo 저장
                // =========================
                artistProfileService.saveProfile(
                        saveDTO
                );

                log.info(
                        "✅ PROFILE BUILD SUCCESS artist={}",
                        artist.getName()
                );

            } catch (Exception e) {

                log.error(
                        "❌ PROFILE BUILD FAIL artist={}",
                        artist.getName(),
                        e
                );
            }
        }

        log.info(
                "🎉 PROFILE BUILD END"
        );
    }
}
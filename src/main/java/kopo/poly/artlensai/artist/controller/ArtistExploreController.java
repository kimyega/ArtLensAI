// ArtistExploreController.java

package kopo.poly.artlensai.artist.controller;

import kopo.poly.artlensai.artist.dto.ArtistExploreDTO;
import kopo.poly.artlensai.artist.dto.ArtistExploreSearchDTO;
import kopo.poly.artlensai.artist.service.IArtistExploreService;
import kopo.poly.artlensai.artist.service.IArtistProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/artist/v1/explore")
public class ArtistExploreController {

    private final IArtistExploreService
            artistExploreService;

    private final IArtistProfileService
            artistProfileService;

    // =========================
    // 작가 탐색 리스트 조회
    // =========================
    @GetMapping("")
    public List<ArtistExploreDTO> getArtistExploreList(

            ArtistExploreSearchDTO pDTO

            ) {

        log.info(
                "🎨 작가 탐색 API 호출"
        );

        return artistExploreService
                .getArtistExploreList(
                        pDTO
                );
    }

    // =========================
    // 작가 이름 자동완성
    // =========================
    @GetMapping("/suggest")
    public List<String> suggestArtistNames(

            @RequestParam String keyword

    ) {

        log.info(
                "🔎 작가 이름 자동완성 API 호출"
        );

        return artistExploreService
                .suggestArtistNames(
                        keyword
                );
    }

    // =========================
    // Mongo -> Elasticsearch 전체 재색인
    // (기존 데이터 백필용, 운영/개발 1회성 호출)
    // =========================
    @PostMapping("/reindex")
    public String reindex() {

        log.info(
                "🔄 작가 검색 색인 재생성 API 호출"
        );

        artistProfileService.reindexAll();

        return "OK";
    }
}
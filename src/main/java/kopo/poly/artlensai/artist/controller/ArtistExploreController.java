// ArtistExploreController.java

package kopo.poly.artlensai.artist.controller;

import kopo.poly.artlensai.artist.dto.ArtistExploreDTO;
import kopo.poly.artlensai.artist.dto.ArtistExploreSearchDTO;
import kopo.poly.artlensai.artist.service.IArtistExploreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/artist/v1/explore")
public class ArtistExploreController {

    private final IArtistExploreService
            artistExploreService;

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
}
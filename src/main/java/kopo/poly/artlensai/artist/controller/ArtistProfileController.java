package kopo.poly.artlensai.artist.controller;

import kopo.poly.artlensai.artist.service.IArtistProfileBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/artist/v1/profile")
public class ArtistProfileController {

    private final IArtistProfileBuildService
            artistProfileBuildService;

    // =========================
    // 작가 프로필 전체 생성
    // =========================
    @PostMapping(
            "/build"
    )
    public String buildProfiles() {

        log.info(
                "🎨 ARTIST PROFILE BUILD START"
        );

        artistProfileBuildService
                .buildProfiles();

        log.info(
                "🎉 ARTIST PROFILE BUILD END"
        );

        return "OK";
    }

    // =========================
    // 프로필 이미지 반환
    // =========================
    @GetMapping("/image")
    public ResponseEntity<Resource> getProfileImage(
            @RequestParam String path
    ) {

        try {

            log.info("이미지 요청 path : {}", path);

            path = path.replace("\\", "/");

            File file = new File(
                    "C:/Users/kimye/Desktop/개인프로젝트/2차 평가/python/"
                            + path
            );

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource =
                    new UrlResource(file.toURI());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);

        } catch (Exception e) {

            log.error("이미지 반환 실패", e);

            return ResponseEntity.internalServerError().build();
        }
    }
}
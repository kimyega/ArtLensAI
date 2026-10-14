package kopo.poly.artlensai.like.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.like.dto.LikeStatusDTO;
import kopo.poly.artlensai.like.dto.LikeToggleResponseDTO;
import kopo.poly.artlensai.like.service.IArtistLikeService;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/like/v1")
public class ArtistLikeController {

    private final IArtistLikeService artistLikeService;

    private final IUserService userService;

    @GetMapping("/artists")
    public ResponseEntity<?> getLikedArtists(

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        log.info("{}.getLikedArtists Start!", getClass().getName());

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        LikeStatusDTO result = artistLikeService.getLikedStatus(user.userId());

        log.info("{}.getLikedArtists End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }

    @PutMapping("/artists/{artistId}")
    public ResponseEntity<?> like(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long artistId

    ) {

        log.info("{}.like Start! artistId={}", getClass().getName(), artistId);

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        LikeToggleResponseDTO result = artistLikeService.like(user.userId(), artistId);

        log.info("{}.like End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }

    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<?> unlike(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long artistId

    ) {

        log.info("{}.unlike Start! artistId={}", getClass().getName(), artistId);

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        LikeToggleResponseDTO result = artistLikeService.unlike(user.userId(), artistId);

        log.info("{}.unlike End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }
}

package kopo.poly.artlensai.like.controller;

import kopo.poly.artlensai.like.dto.LikeStatusDTO;
import kopo.poly.artlensai.like.dto.LikeToggleResponseDTO;
import kopo.poly.artlensai.like.service.IArtistLikeService;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        LikeStatusDTO result = artistLikeService.getLikedStatus(user.userId());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/artists/{artistId}")
    public ResponseEntity<?> like(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long artistId

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        LikeToggleResponseDTO result = artistLikeService.like(user.userId(), artistId);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<?> unlike(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long artistId

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        LikeToggleResponseDTO result = artistLikeService.unlike(user.userId(), artistId);

        return ResponseEntity.ok(result);
    }
}

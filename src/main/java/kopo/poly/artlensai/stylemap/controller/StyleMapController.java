package kopo.poly.artlensai.stylemap.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.stylemap.service.IStyleMapService;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/style-map/v1")
public class StyleMapController {

    private final
    IStyleMapService styleMapService;

    private final
    IUserService userService;

    @GetMapping("/list")
    public ResponseEntity<?> getList(

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        if (jwt == null) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED
            ).build();
        }

        String email =
                jwt.getSubject();

        UserDTO user =
                userService.getUserByEmail(
                        email
                );

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK
                                .series()
                                .name(),
                        styleMapService.getHistory(
                                user.userId()
                        )
                )
        );
    }

    @GetMapping("/artists")
    public ResponseEntity<?> getArtists() {

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK
                                .series()
                                .name(),
                        styleMapService.getArtists()
                )
        );
    }

    @GetMapping("/artistProfiles")
    public ResponseEntity<?> getArtistProfiles() {

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK
                                .series()
                                .name(),
                        styleMapService
                                .getArtistProfiles()
                )
        );
    }

}

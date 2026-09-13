package kopo.poly.artlensai.recommend.controller;

import kopo.poly.artlensai.recommend.dto.RecommendResponseDTO;
import kopo.poly.artlensai.recommend.service.IRecommendService;
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
@RequestMapping("/recommend/v1")
public class RecommendController {

    private final IRecommendService recommendService;

    private final IUserService userService;

    @GetMapping("/artists")
    public ResponseEntity<?> getRecommendations(

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        RecommendResponseDTO result = recommendService.getRecommendations(user.userId());

        return ResponseEntity.ok(result);
    }
}

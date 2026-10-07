package kopo.poly.artlensai.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.user.dto.LoginCheckDTO;
import kopo.poly.artlensai.user.dto.MsgDTO;
import kopo.poly.artlensai.user.dto.PasswordResetDTO;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IJwtTokenService;
import kopo.poly.artlensai.user.service.IUserService;
import kopo.poly.artlensai.user.service.impl.MailService;
import kopo.poly.artlensai.user.service.impl.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/auth/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IJwtTokenService jwtTokenService;
    private final RedisTokenService redisTokenService;
    private final IUserService userService;
    private final MailService mailService;

    @Value("${jwt.token.refresh.rotation-threshold}")
    private long refreshRotationThreshold;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(

            @CookieValue(name = "REFRESH_TOKEN")
            String refreshToken,

            HttpServletResponse response
    ) {

        log.info("{}.refresh Start!", getClass().getName());


        log.info("refreshToken : {}", refreshToken);


        // 1. Refresh Token 검증
        if (!jwtTokenService.validateToken(refreshToken)) {

            MsgDTO dto = MsgDTO.builder()
                    .result(0)
                    .msg("유효하지 않은 Refresh Token 입니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(
                            HttpStatus.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED.series().name(),
                            dto
                    ));
        }

        // 2. JWT 에서 이메일 추출
        String email =
                jwtTokenService.getEmailFromToken(refreshToken);

        // 3. Redis 저장값 조회
        String savedToken =
                redisTokenService.getRefreshToken(email);

        log.info("savedToken : {}", savedToken);

        // 4. Redis 값 존재 여부 확인
        if (savedToken == null) {

            MsgDTO dto = MsgDTO.builder()
                    .result(0)
                    .msg("로그인 정보가 존재하지 않습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(
                            HttpStatus.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED.series().name(),
                            dto
                    ));
        }

        // 5. Redis 값과 쿠키 값 비교
        if (!refreshToken.equals(savedToken)) {

            MsgDTO dto = MsgDTO.builder()
                    .result(0)
                    .msg("Refresh Token이 일치하지 않습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(
                            HttpStatus.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED.series().name(),
                            dto
                    ));
        }

        // 6. 사용자 조회
        UserDTO user =
                userService.getUserByEmail(email);

        // 7. 새로운 Access Token 생성
        String newAccessToken =
                jwtTokenService.generateAccessToken(user);

        // Refresh Token 만료 임박 여부 확인
        Instant expiresAt =
                jwtTokenService.getExpiration(refreshToken);

        long remainSeconds =
                Duration.between(
                        Instant.now(),
                        expiresAt
                ).getSeconds();

        String msg;

        log.info("remainSeconds : {}", remainSeconds);
        log.info("refreshRotationThreshold : {}", refreshRotationThreshold);

        // 8. Refresh Token Rotation
        if (remainSeconds <= refreshRotationThreshold) {

            log.info("Refresh Token Rotation Execute!");

            String newRefreshToken =
                    jwtTokenService.generateRefreshToken(user);

            // Redis 갱신
            redisTokenService.saveRefreshToken(
                    email,
                    newRefreshToken
            );

            // Access + Refresh 쿠키 갱신
            jwtTokenService.writeTokenAsCookie(
                    response,
                    newAccessToken,
                    newRefreshToken
            );

            msg = "Access Token 및 Refresh Token 재발급 완료";

        } else {

            log.info("Access Token Only Reissue");

            // Access 만 재발급
            jwtTokenService.writeAccessTokenAsCookie(
                    response,
                    newAccessToken
            );

            msg = "Access Token 재발급 완료";

        }

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg(msg)
                .build();

        log.info("{}.refresh End!", getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(

            @CookieValue(
                    name = "REFRESH_TOKEN",
                    required = false
            )
            String refreshToken,

            HttpServletResponse response
    ) {

        log.info("{}.logout Start!", getClass().getName());

        // Refresh Token 존재 시 Redis 삭제
        if (refreshToken != null
                && jwtTokenService.validateToken(refreshToken)) {

            String email =
                    jwtTokenService.getEmailFromToken(refreshToken);

            redisTokenService.deleteRefreshToken(email);
        }

        // 쿠키 삭제
        jwtTokenService.deleteTokenCookies(response);

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg("로그아웃되었습니다.")
                .build();

        log.info("{}.logout End!", getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<?> resetRequest(
            @RequestBody UserDTO pDTO
    ) {

        UserDTO user =
                userService.getUserByEmail(
                        pDTO.email()
                );

        if (user == null) {

            MsgDTO dto = MsgDTO.builder()
                    .result(0)
                    .msg("존재하지 않는 이메일입니다.")
                    .build();

            return ResponseEntity.badRequest()
                    .body(
                            CommonResponse.of(
                                    HttpStatus.BAD_REQUEST,
                                    HttpStatus.BAD_REQUEST
                                            .series()
                                            .name(),
                                    dto
                            )
                    );
        }

        String token =
                jwtTokenService
                        .generatePasswordResetToken(user);

        redisTokenService.savePasswordResetToken(
                user.email(),
                token
        );

        String resetLink =
                frontendUrl + "/reset-pw?token="
                        + token;

        mailService.sendPasswordResetMail(
                user.email(),
                resetLink
        );

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg("비밀번호 재설정 메일을 발송했습니다.")
                .build();

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody PasswordResetDTO pDTO
    ) {

        if (!jwtTokenService
                .validateToken(
                        pDTO.token()
                )) {

            return ResponseEntity.status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .build();
        }

        String email =
                jwtTokenService
                        .getEmailFromToken(
                                pDTO.token()
                        );

        String savedToken =
                redisTokenService
                        .getPasswordResetToken(
                                email
                        );

        if (!pDTO.token().equals(savedToken)) {

            return ResponseEntity.status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .build();
        }

        userService.updatePassword(
                email,
                pDTO.newPassword()
        );

        redisTokenService.deletePasswordResetToken(
                email
        );

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg("비밀번호가 변경되었습니다.")
                .build();

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }

    @GetMapping("/password/validate")
    public ResponseEntity<?> validatePasswordResetToken(
            @RequestParam String token
    ) {

        if (!jwtTokenService.validateToken(token)) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED
            ).build();
        }

        String email =
                jwtTokenService.getEmailFromToken(token);

        String savedToken =
                redisTokenService.getPasswordResetToken(email);

        if (savedToken == null
                || !token.equals(savedToken)) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED
            ).build();
        }

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        MsgDTO.builder()
                                .result(1)
                                .msg("유효한 토큰입니다.")
                                .build()
                )
        );
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(
            @AuthenticationPrincipal Jwt jwt
    ) {

        boolean loggedIn = false;

        if (jwt != null) {

            String email =
                    jwt.getSubject();

            UserDTO user =
                    userService.getUserByEmail(email);

            loggedIn =
                    user != null
                            && Boolean.TRUE.equals(
                            user.isActive()
                    );
        }


        LoginCheckDTO dto =
                LoginCheckDTO.builder()
                        .loggedIn(loggedIn)
                        .build();

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }
}

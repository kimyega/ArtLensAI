package kopo.poly.artlensai.user.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 사용자 정보 조회 컨트롤러
 * <p>
 * 목적
 * - 요청에 포함된 JWT(쿠키 또는 Authorization 헤더)에서 사용자 식별자(subject)를 읽고,
 * DB에서 해당 사용자의 상세 정보를 조회하여 반환한다.
 * <p>
 * 전체
 * - SecurityConfig에서 /user/** 경로는 인증을 요구하므로, 정상요청에서는 리소스 서버가
 * 이미 인증을 완료한 상태로 본 메서드에 진입한다. 따라서 일반적으로 jwt는 null이 아니다.
 * - 그래도 방어적으로 jwt == null인 경우에 대한 처리(401)을 넣어 두었다.
 * <p>
 * 참고
 * - @AuthenticationPrincipal Jwt jwt로 JWT를 직접 주입받는다.
 * 대안으로는 @AuthenticationPrincipal(expression = "subject") String userId 사용도 가능하다.
 */
@RestController
@RequestMapping("/user/v1")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    /**
     * 사용자 정보 조회용 서비스
     */
    private final IUserService userService;

    /**
     * 현재 로그인 사용자의 상세 정보를 반환한다.
     * <p>
     * 반환 정책
     * - 성공(인증됨) : 200 OK + CommonResponse<UserDTO>
     * - 인증되지 않음(jwt == null) : 401 Unauthorized
     * <p>
     * 주의
     * - 본 엔드포인트는 조회 성격이므로 GET이 더 REST스럽지만,
     * 쿠키 기반 인증/캐시 회피등의 이유로 POST를 사용할 수도 있다.
     */
    @PostMapping(value = "userInfo")
    public ResponseEntity<CommonResponse<UserDTO>> userInfo(@AuthenticationPrincipal Jwt jwt) throws Exception{
        log.info("{}.userInfo Start!", getClass().getName());

        // 방어적 처리: 보안 설정상 도달하지 않아야 하지만, 혹시 모를 잘못된 호출 대비
        if (jwt == null) {
            log.warn("JWT principal is null - unauthorized access to /user/v1/userInfo");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(
                            HttpStatus.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED.series().name(),
                            UserDTO.builder().build()
                    ));
        }

        // JWT의 subject는 발급 시 userId로 설정되어 있음 (JwtTokenService 참조)
        final String email = jwt.getSubject();

        // 서비스 계층으로 조회 위임
        UserDTO pDTO = UserDTO.builder().email(email).build();

        // 존재하지 않은 경우 빈 DTO로 대체 (프런트 단계에서 null 체크 대신 빈 객체 처리 가능)
        UserDTO user = Optional.ofNullable(userService.getUserByEmail(pDTO.email()))
                .orElseGet(() -> UserDTO.builder().build());

        UserDTO rDTO = UserDTO.builder()
                .userId(user.userId())
                .email(user.email())
                .nickname(user.nickname())
                .isActive(user.isActive())
                .createdAt(user.createdAt())
                .build();

        log.info("{}.userInfo End! {}", getClass().getName(), rDTO);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }


}

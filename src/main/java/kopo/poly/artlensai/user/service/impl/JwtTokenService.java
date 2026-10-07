package kopo.poly.artlensai.user.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import kopo.poly.artlensai.global.auth.UserRole;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IJwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 발급(Access/Refresh) 및 쿠키 저장을 담당하는 구현체
 * <p>
 * 설계 요점
 * - 인코딩은 Spring Security의 Nimbus 기반 JwtEncoder를 사용한다
 * - HS256 대칭키 서명을 사용하며, 알고리즘 헤더를 명시한다.
 * - 클레임:
 * - sub        : email
 * - nickname   : 사용자 표시 이름
 * - roles      : List<String> 권한 목록 (Resource Server에서 ROLE_ 접두사로 매핑)
 * - type       : "access" | "refresh" (토큰 용도 구분)
 * <p>
 * 보안 유의사항
 * - 토큰 클레임에는 개인식별정보(PII)를 과도하게 담지 않는다.
 * - Refresh 토큰을 재발급 시 같이 로테이트하는 정책을 권장한다.
 * - 쿠키는 HttpOnly, Secure을 기본으로 사용하고, SameSite는 환경에 맞게 설정한다.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService implements IJwtTokenService {

    // 토큰 클레임 키 밒 값에 대한 상숫 정의 (오타 방지)
    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    /**
     * Spring Security Nimbus 기반 인코더. JwtConfig에서 키로 초기화됨
     */
    private final JwtEncoder jwtEncoder;

    /**
     * Spring Security Nimbus 기반 디코더. JwtConfig에서 키로 초기화됨
     */
    private final JwtDecoder jwtDecoder;

    /**
     * iss(issuer) 클레임에 들어갈 발급자 식별자
     */
    @Value("${jwt.token.creator}")
    private String issuer;

    /**
     * Access Token TTL(초)
     */
    @Value("${jwt.token.access.valid.time}")
    private long accessTtlSec;

    /**
     * Refresh Token TLL(초)
     */
    @Value("${jwt.token.refresh.valid.time}")
    private long refreshTtlSec;

    /**
     * Access Token을 담는 쿠키 이름
     */
    @Value("${jwt.token.access.name}")
    private String accessCookie;

    /**
     * Refresh Token을 담는 쿠키 이름
     */
    @Value("${jwt.token.refresh.name}")
    private String refreshCookie;

    private final RedisTokenService redisTokenService;


    private static final String TYPE_PASSWORD_RESET =
            "password-reset";

    @Value("${jwt.token.password-reset.valid.time}")
    private long passwordResetTtlSec;

    /**
     * 공통 인코딩 로직
     * - 토큰 유효기간, 타입(access/refresh)만 다르게 설정하여 재사용한다.
     */
    private String encode(UserDTO user, long ttlSec, String type) {
        Instant now = Instant.now();
        List<String> roles = List.of(UserRole.USER.getValue());

        // 클레임 구성
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSec))
                .subject(user.email())
                .claim(CLAIM_NICKNAME, user.nickname())
                .claim(CLAIM_TYPE, type)
                .claim(CLAIM_ROLES, roles)
                .build();

        // HS256 헤더 명시
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // 인코딩 (서명 포함)
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

    /**
     * 권한 문자열(ROLE_ADMIN, ROLE_USER)를 List<String>으로 변환
     * 값이 비어있으면 기본값으로 "USER" 부여
     */
    private static List<String> splitRoles(String roles) {
        if (roles == null || roles.isBlank()) return List.of("USER");
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    /**
     * Access Token 생성
     *
     * @param user 토큰에 반영할 최소 사용자 정보(userId는 필수)
     * @return 서명된 JWT 문자열
     */
    @Override
    public String generateAccessToken(UserDTO user) {
        return encode(user, accessTtlSec, TYPE_ACCESS);
    }

    /**
     * Refresh Token 생성
     *
     * @param user 토큰에 반영할 최소 사용자 정보(userId는 필수)
     * @return 서명된 JWT 문자열
     */
    @Override
    public String generateRefreshToken(UserDTO user) {
        return encode(user, refreshTtlSec, TYPE_REFRESH);
    }


    /**
     * Access/Refresh 토큰을 각각 HttpOnly 쿠키로 저장한다.
     * <p>
     * 쿠키 속성 기본값
     * - httpOnly: true  (JS에서 접근 불가)
     * - secure  : true  (HTTP에서만 전송)
     * - path    : "/"   (전역 경로)
     * - sameSite: "Lax" (크로스 사이트 폼 POST가 필요한 경우 None + Secure 조합 고려)
     * - maxAge  : 토큰 TTL과 동일
     * <p>
     * 주의
     * - 프런트가 다른 도메인/서브도메인일 경우 Domain 설정이 필요할 수 있음.
     * - 프록시/게이트웨이가 있는 경우, 실제 배포 환경에 맞춰 Secure, SameSite, Domain을 점검할 것
     */
    @Override
    public void writeTokenAsCookie(HttpServletResponse res, String accessToken, String refreshToken) {

        writeAccessTokenAsCookie(res, accessToken);

        ResponseCookie rt = ResponseCookie.from(
                        refreshCookie,
                        refreshToken
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(refreshTtlSec)
                .build();

        res.addHeader("Set-Cookie", rt.toString());
    }


    @Override
    public void writeAccessTokenAsCookie(HttpServletResponse res, String accessToken) {

        ResponseCookie at = ResponseCookie.from(
                        accessCookie,
                        accessToken
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(accessTtlSec)
                .build();

        res.addHeader("Set-Cookie", at.toString());
    }

    @Override
    public void issueTokens(UserDTO user, HttpServletResponse res) {

        String accessToken =
                generateAccessToken(user);

        String refreshToken =
                generateRefreshToken(user);

        // Access Token -> HttpOnly Cookie
        writeTokenAsCookie(
                res,
                accessToken,
                refreshToken
        );

        // Refresh Token -> Redis
        redisTokenService.saveRefreshToken(
                user.email(),
                refreshToken
        );

    }

    /**
     * JWT에서 사용자 이메일(subject)을 추출한다.
     * <p>
     * 사용 목적
     * - Refresh Token 재발급 시 사용자 식별
     * - 인증된 JWT로부터 현재 로그인한 사용자의 이메일 조회
     * <p>
     * 동작 방식
     * - JwtDecoder를 통해 JWT를 검증 및 파싱한다.
     * - JWT의 subject(sub) 클레임 값을 반환한다.
     * <p>
     * 참고
     * - 현재 프로젝트에서는 email을 subject(sub)로 사용한다.
     * - 토큰 생성 시 subject(user.email())로 저장된다.
     *
     * @param token JWT 문자열
     * @return JWT의 subject(email)
     */
    @Override
    public String getEmailFromToken(String token) {
        return jwtDecoder
                .decode(token)
                .getSubject();
    }

    /**
     * JWT의 유효성을 검증한다.
     * <p>
     * 검증 항목
     * - JWT 서명(Signature) 검증
     * - 만료시간(exp) 검증
     * - 토큰 형식 검증
     * <p>
     * 반환 정책
     * - 검증 성공 : true
     * - 검증 실패 : false
     * <p>
     * 사용 예시
     * - Refresh Token 재발급 요청 시
     * - Access Token 검증 전 사전 체크
     * <p>
     * 참고
     * - JwtDecoder가 내부적으로 서명 및 만료시간을 검증한다.
     * - 예외 발생 시 false를 반환하여 호출부에서 인증 실패로 처리한다.
     *
     * @param token 검증할 JWT 문자열
     * @return 유효한 JWT이면 true, 아니면 false
     */
    @Override
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public void deleteTokenCookies(HttpServletResponse res) {
        ResponseCookie access = ResponseCookie.from(
                        "ACCESS_TOKEN",
                        ""
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        ResponseCookie refresh = ResponseCookie.from(
                        "REFRESH_TOKEN",
                        ""
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        res.addHeader("Set-Cookie", access.toString());
        res.addHeader("Set-Cookie", refresh.toString());
    }

    @Override
    public Instant getExpiration(String token) {
        return jwtDecoder
                .decode(token)
                .getExpiresAt();
    }

    /**
     * 비밀번호 재설정용 JWT 생성
     *
     * @param user 토큰에 반영할 사용자 정보
     * @return Password Reset JWT
     */
    @Override
    public String generatePasswordResetToken(UserDTO user) {
        return encode(
                user,
                passwordResetTtlSec,
                TYPE_PASSWORD_RESET
        );
    }


}

package kopo.poly.artlensai.global.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import kopo.poly.artlensai.global.jwt.CookieOrHeaderBearerTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    /**
     * HS256 HMAC 서명에 사용할 비밀키 원문
     * - 최소 32바이트 이상을 권장
     * - 운영 환경에선 환경변수/시크릿 매니저에 보관하고, Git에 커밋하지 마세요.
     */
    @Value("${jwt.secret.key}")
    private String secret;

    /**
     * HS256을 SecretKey 빈
     * - 이 키로 JwtEncoder가 토큰에 서명하고 JwtDecoder가 검증합니다.
     * - 알고리즘 표기는 "HmacSHA256" (자바 JCE 이름)을 사용합니다.
     */
    @Bean
    public SecretKey jwtSecretKey() {
        // secret 길이가 짧으면 보안상 취약 및 라이브러리에서 거부될 수 있습니다.
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
    }

    /**
     * JWT 인코더 (발급기)
     *  - NimbusJwtEncoder는 내부적으로 JWTSource를 사용합니다.
     *  - HS256에서는 ImmutableSecret를 JWTSource로 넘겨주는 방식이 버전 호환성이 좋습니다.
     *  <p>
     *  참고 :
     *   - Spring Security 6.3+ 에서는 withSecretKey() 빌더도 제공됩니다.
     *  return NimbusJwtEncoder.withSecretKey(key).build();
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT 디코더 (검증기)
     *  - HS256 대칭기로 서명된 토큰을 검증합니다.
     *  - 만료(exp), 발행(iss) 등은 스프링 시큐리티 기본 검증 + 실제 사용처에서 로직으로 조합하세요.
     *  - macAlgorithm을 명시하면 "alg 불일치" 공격을 방지할 수 있습니다.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * JWT + Spring Security 권한 매핑 설정
     * - JWT의 "roles" 클레임(List<String>)응 읽어 ROLE_ 접두사를 붙여 GrantedAuthority로 변환합니다.
     * - 예) roled: ["USER","ADMIN"] -> 권한: ["ROLE_USER","ROLE_ADMIN"]
     * <p>
     * 주의:
     *  - role 클레임이 문자열 CSV("USER, ADMIN")가 아니라 List<String>이어야 합니다.
     *  - 발급 시 JwtTokenService에서 roles를 List<String>으로 넣고 있습니다.
     *  - 클레임의 이름을 바꾸려면 setAuthoritiesClaimName("...")를 수정하세요.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthorityPrefix("ROLE_");
        delegate.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(delegate);
        return converter;
    }

    /**
     * BearerTokenResolver
     * - 리소스 서버가 요청에서 토큰을 찾을 때의 규칙을 정의합니다.
     * - 우선순위: (1) HttpOnly 쿠키(Access Token) -> (2)  Authorization 헤더(Bearer)
     * - 보안상 쿼리스트링/폼 파라미터로 토큰 전달은 허용하지 않습니다.(Resolver 구현 내부에서 차단)
     * <p>
     * 쿠키 전략 주의:
     * - XSS 방지를 위해 HttpOnly 사용.
     * - CSRF를 완화하려면 SameSite=Lax/Strict + CSRF 토큰(상황에 따라) 고려.
     * - 크로스 도메인 필요 시 SameSite=None + Secure=true 조합 필요.
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver(
            @Value("${jwt.token.access.name}") String accessTokenName) {
        return new CookieOrHeaderBearerTokenResolver(accessTokenName);
    }

}

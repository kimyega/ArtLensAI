package kopo.poly.artlensai.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import kopo.poly.artlensai.global.util.CmmUtil;
import kopo.poly.artlensai.global.util.JwtUtil;
import kopo.poly.artlensai.user.dto.MailDTO;
import kopo.poly.artlensai.user.dto.MsgDTO;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import kopo.poly.artlensai.user.service.impl.MailService;
import kopo.poly.artlensai.user.service.impl.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;
    private final JwtUtil jwtUtil;
    private final MailService mailService;
    private final RedisTokenService redisTokenService;

    @PostMapping("/login")
    public MsgDTO login(@RequestBody UserDTO pDTO) throws Exception {
        log.info("{}.login Start! email: {}", this.getClass().getName(), pDTO.email());

        Optional<UserDTO> userOpt = userService.getUserLogin(pDTO);

        if (userOpt.isPresent()) {
            String token = jwtUtil.generateAccessToken(userOpt.get().email());
            String refreshToken = jwtUtil.generateRefreshToken(userOpt.get().email());

            redisTokenService.saveRefreshToken(
                    userOpt.get().email(),
                    refreshToken
            );

            return MsgDTO.builder()
                    .result(1)
                    .msg("로그인 성공")
                    .token(token)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            return MsgDTO.builder()
                    .result(0)
                    .msg("이메일 또는 비밀번호가 일치하지 않습니다.")
                    .token(null)
                    .refreshToken(null)
                    .build();
        }
    }

    @PostMapping("/signup")
    public MsgDTO signup(@RequestBody UserDTO pDTO) {

        if (!mailService.isVerified(pDTO.email())) {
            return MsgDTO.builder()
                    .result(0)
                    .msg("이메일 인증이 필요합니다.")
                    .code("EMAIL_NOT_VERIFIED")
                    .build();
        }

        try {
            userService.insertUser(pDTO);

            return MsgDTO.builder()
                    .result(1)
                    .msg("회원가입 성공")
                    .build();

        } catch (RuntimeException e) {

            if (e.getMessage().contains("이미 가입된 이메일")) {
                return MsgDTO.builder()
                        .result(0)
                        .msg(e.getMessage())
                        .code("EMAIL_DUPLICATE")
                        .build();
            }

            return MsgDTO.builder()
                    .result(0)
                    .msg("회원가입 실패")
                    .code("UNKNOWN_ERROR")
                    .build();
        }
    }

    @PostMapping("/checkEmail")
    public MsgDTO checkEmail(@RequestBody UserDTO pDTO) {

        log.info("{}.checkEmail Start! email: {}", this.getClass().getName(), pDTO.email());

        boolean exists = userService.existsEmail(pDTO.email());

        log.info("{}.checkEmail End! exists: {}", this.getClass().getName(), exists);

        if (exists) {
            return MsgDTO.builder()
                    .result(0)
                    .msg("이미 가입된 이메일입니다.")
                    .code("EMAIL_DUPLICATE")
                    .build();
        }

        return MsgDTO.builder()
                .result(1)
                .msg("사용 가능한 이메일입니다.")
                .build();
    }


    // 🔥 이메일 인증번호 발송
    @PostMapping("/sendCode")
    public MsgDTO sendCode(@RequestBody MailDTO pDTO) {

        if (userService.existsEmail(pDTO.email())) {
            return MsgDTO.builder()
                    .result(0)
                    .msg("이미 가입된 이메일입니다.")
                    .code("EMAIL_DUPLICATE")
                    .build();
        }

        mailService.sendCode(pDTO.email());

        return MsgDTO.builder()
                .result(1)
                .msg("인증번호 발송 완료")
                .build();
    }

    // 🔥 인증번호 검증
    @PostMapping("/verifyCode")
    public MsgDTO verifyCode(@RequestBody MailDTO pDTO) {

        boolean ok = mailService.verifyCode(pDTO.email(), pDTO.code());

        if (ok) {
            return MsgDTO.builder()
                    .result(1)
                    .msg("인증 성공")
                    .build();
        }

        return MsgDTO.builder()
                .result(0)
                .msg("인증 실패")
                .build();
    }

    @GetMapping("/profile")
    public MsgDTO profile(HttpServletRequest request) {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("토큰 없음");
        }

        String jwt = token.substring(7);

        String email = jwtUtil.getEmailFromToken(jwt);

        if (email == null) {
            throw new RuntimeException("토큰 만료 또는 위조");
        }

        UserDTO user = userService.getUserByEmail(email);

        if (user == null) {
            throw new RuntimeException("유저 없음");
        }

        return MsgDTO.builder()
                .result(1)
                .msg("OK")
                .dto(user)
                .build();
    }

    @PostMapping("/findPw")
    public MsgDTO findPw(@RequestBody UserDTO pDTO) {

        String email = pDTO.email();

        if (email == null || email.isBlank()) {
            throw new RuntimeException("이메일을 입력하세요.");
        }

        if (!userService.existsEmail(email)) {
            throw new RuntimeException("가입되지 않은 이메일입니다.");
        }

        String token = java.util.UUID.randomUUID().toString();

        mailService.saveResetToken(email, token);

        String link = "http://localhost:5173/reset-pw?token=" + token;

        mailService.sendResetEmail(email, link);

        return MsgDTO.builder()
                .result(1)
                .msg("비밀번호 재설정 메일 발송 완료")
                .build();
    }

    @PostMapping("/resetPassword")
    public MsgDTO resetPassword(@RequestBody Map<String, String> req) {

        String token = req.get("token");
        String newPassword = req.get("newPassword");

        if (token == null || newPassword == null) {
            throw new RuntimeException("잘못된 요청입니다.");
        }

        String email = mailService.getEmailByToken(token);

        if (email == null) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        userService.updatePassword(email, newPassword);

        mailService.removeToken(token);

        return MsgDTO.builder()
                .result(1)
                .msg("비밀번호 변경 완료")
                .build();
    }

    @GetMapping("/validateResetToken")
    public MsgDTO validateResetToken(@RequestParam String token) {

        String email = mailService.getEmailByToken(token);

        if (email == null) {
            return MsgDTO.builder()
                    .result(0)
                    .msg("INVALID_TOKEN")
                    .build();
        }

        return MsgDTO.builder()
                .result(1)
                .msg("VALID_TOKEN")
                .build();
    }

    @PostMapping("/refresh")
    public MsgDTO refresh(
            @RequestBody Map<String, String> req
    ) {

        String refreshToken =
                req.get("refreshToken");

        // 1. refreshToken 검증
        if (!jwtUtil.isTokenValid(refreshToken)) {

            return MsgDTO.builder()
                    .result(0)
                    .msg("INVALID_REFRESH_TOKEN")
                    .build();
        }

        // 2. 이메일 추출
        String email =
                jwtUtil.getEmailFromToken(refreshToken);

        // 3. Redis 저장값 조회
        String savedToken =
                redisTokenService.getRefreshToken(email);

        // 4. Redis 비교
        if (savedToken == null ||
                !savedToken.equals(refreshToken)) {

            return MsgDTO.builder()
                    .result(0)
                    .msg("INVALID_REFRESH_TOKEN")
                    .build();
        }

        // 5. 새 AccessToken 발급
        String newAccessToken =
                jwtUtil.generateAccessToken(email);

        return MsgDTO.builder()
                .result(1)
                .msg("SUCCESS")
                .token(newAccessToken)
                .build();
    }

}

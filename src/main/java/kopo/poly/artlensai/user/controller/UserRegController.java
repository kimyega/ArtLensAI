package kopo.poly.artlensai.user.controller;

import jakarta.validation.Valid;
import kopo.poly.artlensai.global.auth.UserRole;
import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.user.dto.MailDTO;
import kopo.poly.artlensai.user.dto.MsgDTO;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import kopo.poly.artlensai.user.service.impl.MailService;
import kopo.poly.artlensai.user.service.impl.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value = "/reg/v1")
@RequiredArgsConstructor
@RestController
public class UserRegController {

    private final IUserService userService;

    // Spring Security에서 제공하는 비밀번호 암호화 객체 (Bcrypt 해시 필수)
    private final PasswordEncoder bCryptPasswordEncoder;

    private final MailService mailService;

    private final RedisTokenService redisTokenService;

    /**
     * 입력된 아이디의 존재 여부를 체크하는 API
     *
     * @param pDTO 사용자로부터 전달받은 ID
     * @return 존재 여부를 담은 UserDTO
     */
    @PostMapping(value = "getEmailExists")
    public ResponseEntity<CommonResponse<UserDTO>> getEmailExists(@RequestBody UserDTO pDTO)
            throws Exception {

        log.info("{}.getEmailExists Start!", getClass().getName());

        UserDTO rDTO = userService.getEmailExists(pDTO);

        log.info("{}.getEmailExists End! {}", getClass().getName(), rDTO);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }

    /**
     * 사용자 회원가입 처리 API
     * - 유효성 검증 처리
     * - 패스워드 암호화
     * - 가입 실패/성공 메시지 반환
     *
     */
    @PostMapping(value = "insertUser")
    public ResponseEntity<?> insertUser(@Valid @RequestBody UserDTO pDTO, BindingResult bindingResult) {

        log.info("{}.insertUser Start!", getClass().getName());


        // 1. 유효성 검증 실패 시 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return CommonResponse.getErrors(bindingResult);
        }


        // 이메일 인증 여부 확인
        if (!redisTokenService.isEmailVerified(
                pDTO.email()
        )) {

            MsgDTO dto = MsgDTO.builder()
                    .result(0)
                    .msg("이메일 인증이 필요합니다.")
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


        int res = 0;        // 회원가입 처리 결과 코드
        String msg = "";    // 처리 경과 메시지
        MsgDTO dto;         // 응단 메시지 객체

        log.info("pDTO: {}", pDTO);

        try {
            // 2. 전달받은 회원 정보에 비밀번호 암호화 및 권한 추가
            UserDTO nDTO = UserDTO.createUser(
                    pDTO,
                    bCryptPasswordEncoder.encode(pDTO.password())
            );

            // 3. 회원가입 처리
            res = userService.insertUser(nDTO);

            log.info("회원가입 결과(res) : {}", res);

            // 4. 처리 결과 메시지 구성
            if (res == 1) {

                redisTokenService.deleteEmailVerified(
                        pDTO.email()
                );

                msg = "회원가입되었습니다.";
            } else if (res == 2) {
                msg = "이미 가입된 아이디입니다.";
            } else {
                msg = "오류로 인해 회원가입이 실패하였습니다.";
            }
        } catch (Exception e) {
            msg = "실패하였습니다. : " + e;
            res = 2;
            log.error("회원가입 중 예외 발생", e);
        }  finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();
            log.info("{}.insertUser End! {}", getClass().getName(), dto);
        }

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto)
        );
    }


    @PostMapping("sendCode")
    public ResponseEntity<?> sendCode(
            @RequestBody MailDTO pDTO
    ) {

        log.info("{}.sendCode Start!", getClass().getName());

        String email = pDTO.email();

        String code =
                String.valueOf(
                        (int) (
                                Math.random() * 900000
                        ) + 100000
                );

        redisTokenService.saveEmailCode(
                email,
                code
        );

        mailService.sendVerificationCode(
                email,
                code
        );

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg("인증번호를 발송했습니다.")
                .build();

        log.info("{}.sendCode End!", getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }

    @PostMapping("verifyCode")
    public ResponseEntity<?> verifyCode(
            @RequestBody MailDTO pDTO
    ) {

        log.info("{}.verifyCode Start!", getClass().getName());

        String email = pDTO.email();
        String code = pDTO.code();

        String savedCode =
                redisTokenService.getEmailCode(
                        email
                );

        int result;
        String msg;

        if (
                savedCode != null &&
                        savedCode.equals(code)
        ) {

            redisTokenService.saveEmailVerified(
                    email
            );

            redisTokenService.deleteEmailCode(
                    email
            );

            result = 1;
            msg = "이메일 인증 성공";

        } else {

            result = 0;
            msg = "인증번호가 일치하지 않습니다.";
        }

        MsgDTO dto = MsgDTO.builder()
                .result(result)
                .msg(msg)
                .build();

        log.info("{}.verifyCode End!", getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        dto
                )
        );
    }
}

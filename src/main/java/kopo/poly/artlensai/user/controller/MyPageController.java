package kopo.poly.artlensai.user.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.user.dto.MyPageStatDTO;
import kopo.poly.artlensai.user.dto.PasswordChangeDTO;
import kopo.poly.artlensai.user.dto.UpdateNicknameDTO;
import kopo.poly.artlensai.user.service.IMyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mypage/v1")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final IMyPageService myPageService;

    @PostMapping("stat")
    public ResponseEntity<CommonResponse<MyPageStatDTO>>
    stat(
            @AuthenticationPrincipal Jwt jwt
    ) throws Exception {

        String email = jwt.getSubject();

        MyPageStatDTO rDTO =
                myPageService.getStat(email);

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        rDTO
                )
        );
    }

    @PostMapping("nickname")
    public ResponseEntity<CommonResponse<Integer>>
    updateNickname(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateNicknameDTO pDTO
    ) throws Exception {

        String email = jwt.getSubject();

        int res = myPageService.updateNickname(
                email,
                pDTO.nickname()
        );

        if (res < 1) {

            return ResponseEntity.ok(
                    CommonResponse.of(
                            HttpStatus.BAD_REQUEST,
                            HttpStatus.BAD_REQUEST
                                    .series()
                                    .name(),
                            0
                    )
            );
        }

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        1
                )
        );
    }

    @PostMapping("password")
    public ResponseEntity<CommonResponse<Integer>>
    changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PasswordChangeDTO pDTO
    ) throws Exception {

        String email = jwt.getSubject();

        int res =
                myPageService.changePassword(
                        email,
                        pDTO.currentPassword(),
                        pDTO.newPassword()
                );

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        res
                )
        );
    }


    @PostMapping("withdraw")
    public ResponseEntity<CommonResponse<Integer>>
    withdraw(
            @AuthenticationPrincipal Jwt jwt
    ) throws Exception {

        String email = jwt.getSubject();

        int res =
                myPageService.withdraw(
                        email
                );

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        res
                )
        );
    }

}

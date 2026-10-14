package kopo.poly.artlensai.tastetest.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.tastetest.dto.TasteAnswerRequestDTO;
import kopo.poly.artlensai.tastetest.dto.TasteTestResponseDTO;
import kopo.poly.artlensai.tastetest.service.ITasteTestService;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/taste-test/v1")
public class TasteTestController {

    private final ITasteTestService tasteTestService;

    private final IUserService userService;

    @PostMapping("/start")
    public ResponseEntity<?> start(

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        log.info("{}.start Start!", getClass().getName());

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        TasteTestResponseDTO result = tasteTestService.startTest(user.userId());

        log.info("{}.start End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }

    @PostMapping("/{testId}/answer")
    public ResponseEntity<?> answer(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long testId,

            @RequestBody
            TasteAnswerRequestDTO reqDTO

    ) {

        log.info("{}.answer Start! testId={}", getClass().getName(), testId);

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        TasteTestResponseDTO result = tasteTestService.submitAnswer(testId, user.userId(), reqDTO);

        log.info("{}.answer End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }
}

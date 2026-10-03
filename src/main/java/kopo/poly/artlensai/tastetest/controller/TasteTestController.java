package kopo.poly.artlensai.tastetest.controller;

import kopo.poly.artlensai.tastetest.dto.TasteAnswerRequestDTO;
import kopo.poly.artlensai.tastetest.dto.TasteTestResponseDTO;
import kopo.poly.artlensai.tastetest.service.ITasteTestService;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        TasteTestResponseDTO result = tasteTestService.startTest(user.userId());

        return ResponseEntity.ok(result);
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

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        TasteTestResponseDTO result = tasteTestService.submitAnswer(testId, user.userId(), reqDTO);

        return ResponseEntity.ok(result);
    }
}

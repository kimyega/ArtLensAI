package kopo.poly.artlensai.share.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.share.dto.CreateShareRequestDTO;
import kopo.poly.artlensai.share.dto.ShareLinkDTO;
import kopo.poly.artlensai.share.dto.SharedAnalysisResultDTO;
import kopo.poly.artlensai.share.service.IShareService;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/share/v1")
public class ShareController {

    private final IShareService shareService;

    private final IUserService userService;

    @PostMapping("")
    public ResponseEntity<?> createShare(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestBody
            CreateShareRequestDTO req

    ) {

        log.info("{}.createShare Start! resultId={}", getClass().getName(), req.resultId());

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        ShareLinkDTO result =
                shareService.createOrGetShare(user.userId(), req.resultId(), req.expiresInDays());

        log.info("{}.createShare End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMyShare(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam
            String resultId

    ) {

        log.info("{}.getMyShare Start! resultId={}", getClass().getName(), resultId);

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        Optional<ShareLinkDTO> result = shareService.getMyShare(user.userId(), resultId);

        log.info("{}.getMyShare End! present={}", getClass().getName(), result.isPresent());

        if (result.isEmpty()) {

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result.get())
        );
    }

    @GetMapping("/mine/batch")
    public ResponseEntity<?> getMyShares(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam
            List<String> resultIds

    ) {

        log.info("{}.getMyShares Start! resultIdCount={}", getClass().getName(), resultIds.size());

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        Map<String, ShareLinkDTO> result = shareService.getMyShares(user.userId(), resultIds);

        log.info("{}.getMyShares End! resultCount={}", getClass().getName(), result.size());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> revoke(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long shareId

    ) {

        log.info("{}.revoke Start! shareId={}", getClass().getName(), shareId);

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        shareService.revoke(user.userId(), shareId);

        log.info("{}.revoke End! shareId={}", getClass().getName(), shareId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getPublicResult(

            @PathVariable
            String token

    ) {

        log.info("{}.getPublicResult Start! token={}", getClass().getName(), token);

        SharedAnalysisResultDTO result = shareService.getPublicResult(token);

        log.info("{}.getPublicResult End! {}", getClass().getName(), result);

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }
}

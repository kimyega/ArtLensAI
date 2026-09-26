package kopo.poly.artlensai.share.controller;

import kopo.poly.artlensai.share.dto.CreateShareRequestDTO;
import kopo.poly.artlensai.share.dto.ShareLinkDTO;
import kopo.poly.artlensai.share.dto.SharedAnalysisResultDTO;
import kopo.poly.artlensai.share.exception.ShareAccessDeniedException;
import kopo.poly.artlensai.share.exception.ShareNotFoundException;
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

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        try {

            ShareLinkDTO result =
                    shareService.createOrGetShare(user.userId(), req.resultId(), req.expiresInDays());

            return ResponseEntity.ok(result);

        } catch (ShareAccessDeniedException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (ShareNotFoundException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMyShare(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam
            String resultId

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        return shareService.getMyShare(user.userId(), resultId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/mine/batch")
    public ResponseEntity<?> getMyShares(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam
            List<String> resultIds

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        Map<String, ShareLinkDTO> result = shareService.getMyShares(user.userId(), resultIds);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> revoke(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long shareId

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user = userService.getUserByEmail(jwt.getSubject());

        try {

            shareService.revoke(user.userId(), shareId);

            return ResponseEntity.noContent().build();

        } catch (ShareNotFoundException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getPublicResult(

            @PathVariable
            String token

    ) {

        try {

            SharedAnalysisResultDTO result = shareService.getPublicResult(token);

            return ResponseEntity.ok(result);

        } catch (ShareNotFoundException e) {

            return ResponseEntity.notFound().build();
        }
    }
}

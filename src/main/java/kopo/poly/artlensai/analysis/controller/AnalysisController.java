package kopo.poly.artlensai.analysis.controller;

import jakarta.servlet.http.HttpServletRequest;
import kopo.poly.artlensai.analysis.dto.AnalysisDTO;
import kopo.poly.artlensai.analysis.dto.AnalysisStartResponseDTO;
import kopo.poly.artlensai.analysis.dto.AnalysisUsageDTO;
import kopo.poly.artlensai.analysis.dto.MsgDTO;
import kopo.poly.artlensai.analysis.exception.AnalysisLimitExceededException;
import kopo.poly.artlensai.analysis.service.IAnalysisUsageService;
import kopo.poly.artlensai.analysis.service.impl.AnalysisService;
import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/analysis/v1")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final IUserService userService;
    private final AnalysisService analysisService;
    private final IAnalysisUsageService analysisUsageService;

    private final S3Client s3Client;

    @Value("${ncp.bucket}")
    private String bucket;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(

            @RequestPart("file")
            MultipartFile file,

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        log.info("jwt={}", jwt);
        log.info("file={}", file.getOriginalFilename());

        try {

            if (jwt == null) {

                return ResponseEntity.status(
                        HttpStatus.UNAUTHORIZED
                ).build();
            }

            String email =
                    jwt.getSubject();

            UserDTO user =
                    userService.getUserByEmail(
                            email
                    );

            Long uploadId =
                    analysisService.saveUploadImage(
                            file,
                            user
                    );

            MsgDTO dto =
                    MsgDTO.builder()
                            .result(1)
                            .msg("업로드 성공")
                            .dto(uploadId)
                            .build();

            return ResponseEntity.ok(
                    CommonResponse.of(
                            HttpStatus.OK,
                            HttpStatus.OK.series().name(),
                            dto
                    )
            );

        } catch (Exception e) {

            log.error(
                    "uploadImage error",
                    e
            );

            MsgDTO dto =
                    MsgDTO.builder()
                            .result(0)
                            .msg("업로드 실패")
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
    }

    @PostMapping("/start")
    public ResponseEntity<?> startAnalysis(

            @RequestBody
            AnalysisDTO req,

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        try {

            String email =
                    jwt.getSubject();

            UserDTO user =
                    userService.getUserByEmail(
                            email
                    );

            AnalysisStartResponseDTO result =
                    analysisService.startAnalysis(
                            req.uploadId(),
                            user
                    );

            MsgDTO dto =
                    MsgDTO.builder()
                            .result(1)
                            .msg("분석 성공")
                            .dto(result)
                            .build();

            return ResponseEntity.ok(
                    CommonResponse.of(
                            HttpStatus.OK,
                            HttpStatus.OK.series().name(),
                            dto
                    )
            );

        } catch (AnalysisLimitExceededException e) {

            log.info("analysis limit exceeded: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(
                            CommonResponse.of(
                                    HttpStatus.TOO_MANY_REQUESTS,
                                    HttpStatus.TOO_MANY_REQUESTS
                                            .series()
                                            .name(),
                                    MsgDTO.builder()
                                            .result(0)
                                            .msg(e.getMessage())
                                            .dto(e.getUsage())
                                            .build()
                            )
                    );

        } catch (Exception e) {

            log.error(
                    "startAnalysis error",
                    e
            );

            return ResponseEntity.badRequest()
                    .body(
                            CommonResponse.of(
                                    HttpStatus.BAD_REQUEST,
                                    HttpStatus.BAD_REQUEST
                                            .series()
                                            .name(),
                                    MsgDTO.builder()
                                            .result(0)
                                            .msg("분석 실패")
                                            .build()
                            )
                    );
        }
    }

    @GetMapping("/usage")
    public ResponseEntity<?> getUsage(

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        if (jwt == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO user =
                userService.getUserByEmail(jwt.getSubject());

        AnalysisUsageDTO usage =
                analysisUsageService.getUsage(user.userId());

        return ResponseEntity.ok(
                CommonResponse.of(
                        HttpStatus.OK,
                        HttpStatus.OK.series().name(),
                        usage
                )
        );
    }

    @GetMapping("/result/{id}")
    public ResponseEntity<?> getResult(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                analysisService.getAnalysisResult(id)
        );
    }

    @GetMapping("/api/image/**")
    public ResponseEntity<byte[]> getImage(
            HttpServletRequest request
    ) {

        try {

            String key =
                    request.getRequestURI()
                            .replace(
                                    "/analysis/v1/api/image/",
                                    ""
                            );

            ResponseBytes<GetObjectResponse> obj =
                    s3Client.getObjectAsBytes(

                            GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build()
                    );

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.IMAGE_JPEG
                    )
                    .body(
                            obj.asByteArray()
                    );

        } catch (Exception e) {

            log.error(
                    "image load fail",
                    e
            );

            return ResponseEntity.notFound().build();
        }
    }
}
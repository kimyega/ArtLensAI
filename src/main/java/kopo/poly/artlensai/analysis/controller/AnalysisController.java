package kopo.poly.artlensai.analysis.controller;

import kopo.poly.artlensai.analysis.dto.AnalysisDTO;
import kopo.poly.artlensai.analysis.dto.MsgDTO;
import kopo.poly.artlensai.analysis.service.impl.AnalysisService;
import kopo.poly.artlensai.global.util.JwtUtil;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final JwtUtil jwtUtil;
    private final IUserService userService;
    private final AnalysisService analysisService;

    // 🔥 이미지 업로드 (핵심)
    @PostMapping("/upload")
    public MsgDTO uploadImage(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request
    ) {

        log.info("{}.uploadImage Start!", this.getClass().getName());

        try {

            // =========================
            // 1. JWT → user 추출
            // =========================
            String token = request.getHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new RuntimeException("토큰 없음");
            }

            String email = jwtUtil.getEmailFromToken(token.substring(7));

            UserDTO userDTO = userService.getUserByEmail(email);

            if (userDTO == null) {
                throw new RuntimeException("유저 없음");
            }

            // =========================
            // 2. 서비스로 파일 전달
            // =========================
            Long uploadId = analysisService.saveUploadImage(file, userDTO);

            return MsgDTO.builder()
                    .result(1)
                    .msg("업로드 성공")
                    .dto(uploadId)
                    .build();

        } catch (Exception e) {

            log.error("uploadImage error", e);

            return MsgDTO.builder()
                    .result(0)
                    .msg("업로드 실패")
                    .build();
        }
    }

    @PostMapping("/start")
    public MsgDTO startAnalysis(
            @RequestBody AnalysisDTO req,
            HttpServletRequest request
    ) {

        log.info("{}.startAnalysis Start!", this.getClass().getName());

        try {

            // =========================
            // 1. JWT → user 추출
            // =========================
            String token = request.getHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new RuntimeException("토큰 없음");
            }

            String email = jwtUtil.getEmailFromToken(token.substring(7));

            UserDTO userDTO = userService.getUserByEmail(email);

            if (userDTO == null) {
                throw new RuntimeException("유저 없음");
            }

            // =========================
            // 2. uploadId 추출
            // =========================
            Long uploadId = req.uploadId();

            if (uploadId == null) {
                throw new RuntimeException("uploadId 없음");
            }

            // =========================
            // 3. 분석 시작 (FastAPI 호출)
            // =========================
            analysisService.startAnalysis(uploadId, userDTO);

            return MsgDTO.builder()
                    .result(1)
                    .msg("분석 시작 성공")
                    .build();

        } catch (Exception e) {

            log.error("startAnalysis error", e);

            return MsgDTO.builder()
                    .result(0)
                    .msg("분석 시작 실패")
                    .build();
        }
    }
}
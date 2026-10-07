package kopo.poly.artlensai.history.controller;

import kopo.poly.artlensai.global.response.CommonResponse;
import kopo.poly.artlensai.history.dto.DeleteHistoryDTO;
import kopo.poly.artlensai.history.dto.MsgDTO;
import kopo.poly.artlensai.history.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history/v1")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {

    private final IHistoryService historyService;


    @DeleteMapping(value = "/delete")
    public ResponseEntity<?> deleteHistory(

            @RequestBody
            DeleteHistoryDTO req

    ) {

        try {

            historyService.deleteHistory(
                    req.ids()
            );

            MsgDTO dto =
                    MsgDTO.builder()
                            .result(1)
                            .msg("삭제 성공")
                            .build();

            return ResponseEntity.ok(
                    CommonResponse.of(
                            HttpStatus.OK,
                            HttpStatus.OK
                                    .series()
                                    .name(),
                            dto
                    )
            );

        } catch (Exception e) {

            log.error(
                    "deleteHistory error",
                    e
            );

            MsgDTO dto =
                    MsgDTO.builder()
                            .result(0)
                            .msg("삭제 실패")
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
}

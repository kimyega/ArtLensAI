package kopo.poly.artlensai.global.exception;

import kopo.poly.artlensai.user.dto.MsgDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MsgDTO> handle(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MsgDTO.builder()
                        .result(0)
                        .msg("잘못된 요청 파라미터입니다.")
                        .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<MsgDTO> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(MsgDTO.builder()
                        .result(0)
                        .msg(e.getReason())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MsgDTO> handle(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MsgDTO.builder()
                        .result(0)
                        .msg(e.getMessage())
                        .build());
    }
}

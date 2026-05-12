package kopo.poly.artlensai.global.exception;

import kopo.poly.artlensai.user.dto.MsgDTO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public MsgDTO handle(RuntimeException e) {
        return MsgDTO.builder()
                .result(0)
                .msg(e.getMessage())
                .build();
    }
}

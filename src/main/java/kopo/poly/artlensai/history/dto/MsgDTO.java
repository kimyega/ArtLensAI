package kopo.poly.artlensai.history.dto;

import lombok.Builder;

@Builder
public record MsgDTO(
        int result,      // 성공 : 1 / 실패 : 그 외
        String msg,      // 메시지
        String token,     // JWT 토큰 (로그인 성공 시)
        String code,
        Object dto
) {}

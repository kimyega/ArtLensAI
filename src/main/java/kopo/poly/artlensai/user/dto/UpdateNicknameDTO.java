package kopo.poly.artlensai.user.dto;

import lombok.Builder;

@Builder
public record UpdateNicknameDTO(
        String nickname
) {
}

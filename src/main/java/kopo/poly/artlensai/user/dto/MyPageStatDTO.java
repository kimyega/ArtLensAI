package kopo.poly.artlensai.user.dto;

import lombok.Builder;

@Builder
public record MyPageStatDTO(
        Long analysisCount,
        String mainTag
) {
}

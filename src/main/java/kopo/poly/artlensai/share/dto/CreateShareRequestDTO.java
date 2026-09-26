package kopo.poly.artlensai.share.dto;

import lombok.Builder;

@Builder
public record CreateShareRequestDTO(

        String resultId,

        Integer expiresInDays

) {
}

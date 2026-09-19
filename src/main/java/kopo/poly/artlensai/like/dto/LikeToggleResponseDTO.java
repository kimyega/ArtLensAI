package kopo.poly.artlensai.like.dto;

import lombok.Builder;

@Builder
public record LikeToggleResponseDTO(

        Long artistId,

        boolean liked,

        long likeCount

) {
}

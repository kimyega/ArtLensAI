package kopo.poly.artlensai.like.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LikeStatusDTO(

        List<Long> artistIds,

        long likeCount

) {
}

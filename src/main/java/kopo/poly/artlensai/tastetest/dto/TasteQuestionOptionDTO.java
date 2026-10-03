package kopo.poly.artlensai.tastetest.dto;

import lombok.Builder;

@Builder
public record TasteQuestionOptionDTO(

        Long imgId,

        Long artistId,

        String imgUrl
) {
}

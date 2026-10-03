package kopo.poly.artlensai.tastetest.dto;

import java.util.List;

public record TasteAnswerRequestDTO(

        List<Long> shownImgIds,

        Long pickedImgId
) {
}

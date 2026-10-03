package kopo.poly.artlensai.tastetest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record TasteTestResponseDTO(

        Long testId,

        boolean finished,

        Integer questionNo,

        Integer totalQuestions,

        List<TasteQuestionOptionDTO> options,

        String clusterLabel,

        List<TasteResultArtistDTO> topArtists
) {
}

package kopo.poly.artlensai.recommend.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecommendResponseDTO(

        RecommendSource source,

        List<RecommendedArtistDTO> items

) {
}

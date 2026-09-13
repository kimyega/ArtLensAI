package kopo.poly.artlensai.recommend.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecommendResponseDTO(

        String source,

        List<RecommendedArtistDTO> items

) {

    public static final String SOURCE_PERSONALIZED = "PERSONALIZED";
    public static final String SOURCE_POPULAR_FALLBACK = "POPULAR_FALLBACK";
}

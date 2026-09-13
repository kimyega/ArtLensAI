package kopo.poly.artlensai.recommend.dto;

import lombok.Builder;

@Builder
public record RecommendedArtistDTO(

        Long artistId,

        Integer similarityPercent,

        Long reasonArtistId,

        String reasonArtistName,

        Integer postCnt,

        boolean fallback

) {
}

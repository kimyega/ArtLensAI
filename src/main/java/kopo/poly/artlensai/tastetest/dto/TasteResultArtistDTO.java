package kopo.poly.artlensai.tastetest.dto;

import lombok.Builder;

@Builder
public record TasteResultArtistDTO(

        Long artistId,

        String artistName,

        String profileImg,

        int similarityPercent
) {
}

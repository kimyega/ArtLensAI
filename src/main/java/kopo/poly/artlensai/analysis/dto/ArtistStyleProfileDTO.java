package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ArtistStyleProfileDTO(

        Long artistId,

        String artistName,

        String styleDesc,

        List<String> colors,

        List<String> tags,

        String profileImg
) {
}
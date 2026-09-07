package kopo.poly.artlensai.stylemap.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ArtistProfileDTO(

        Long artistId,

        String artistName,

        String profileImg,

        String styleDesc,

        List<String> tags

) {
}

package kopo.poly.artlensai.artist.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ArtistProfileResponseDTO(

        String styleDesc,

        List<String> colors,

        List<String> tags,

        String profileImg
) {
}
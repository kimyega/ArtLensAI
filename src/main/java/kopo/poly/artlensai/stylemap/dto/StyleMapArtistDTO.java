package kopo.poly.artlensai.stylemap.dto;

import lombok.Builder;

@Builder
public record StyleMapArtistDTO(

        Long artistId,

        Double x,

        Double y,

        String clusterLabel

) {
}

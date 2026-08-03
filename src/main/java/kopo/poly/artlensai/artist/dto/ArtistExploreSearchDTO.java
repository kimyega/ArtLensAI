package kopo.poly.artlensai.artist.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ArtistExploreSearchDTO (
        String keyword,

        List<String> style,

        List<String> color,

        List<String> lighting,

        List<String> composition,

        List<String> mood

) {
}

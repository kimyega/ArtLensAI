// ArtistExploreDTO.java

package kopo.poly.artlensai.artist.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ArtistExploreDTO(

        Long artistId,

        String artistName,

        String source,

        String sourceUrl,

        String profileImg,

        String styleDesc,

        List<String> tags,

        List<String> colors

) {
}
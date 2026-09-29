package kopo.poly.artlensai.artist.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ArtistProfileRequestDTO(

        Integer artistId,

        String artistName,

        List<String> imageUrls
) {
}
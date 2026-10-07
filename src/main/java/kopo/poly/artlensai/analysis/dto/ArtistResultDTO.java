package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record ArtistResultDTO(
        Long artistId,

        String artistName,

        Double similarity,

        String profileImg,

        String mainTag
) {}

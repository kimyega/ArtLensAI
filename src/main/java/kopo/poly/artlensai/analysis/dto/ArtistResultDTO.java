package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder
public record ArtistResultDTO(
        Long artistId,
        String artistName,
        Double similarity
) {}

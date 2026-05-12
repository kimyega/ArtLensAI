package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AnalysisResultDTO (
        Integer result,
        List<ArtistResultDTO> top5,
        List<String> tags
) {
}

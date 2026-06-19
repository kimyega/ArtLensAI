package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record AnalysisResultDTO (

        Integer result,

        List<PaletteDTO> palette,

        List<TagResultDTO> tags,

        List<ArtistResultDTO> top5,

        StyleMapDTO styleMap,

        String title,

        String comment

) {
}

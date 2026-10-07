package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder
public record PaletteDTO (
        Integer r,
        Integer g,
        Integer b
){
}

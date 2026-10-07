package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder
public record TagResultDTO (
        String tag,

        String category,

        Double score,

        Integer displayPercent
) {

}

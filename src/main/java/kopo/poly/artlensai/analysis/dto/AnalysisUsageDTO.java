package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder
public record AnalysisUsageDTO(

        int used,

        int remaining,

        int limit

) {
}

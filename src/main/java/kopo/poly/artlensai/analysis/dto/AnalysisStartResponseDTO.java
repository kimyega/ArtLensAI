package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder
public record AnalysisStartResponseDTO(

        Integer result,

        String resultId

) {
}
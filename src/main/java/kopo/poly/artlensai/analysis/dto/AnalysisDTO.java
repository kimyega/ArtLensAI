package kopo.poly.artlensai.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Builder
public record AnalysisDTO (

        Long uploadId
) {
}

package kopo.poly.artlensai.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Builder
public record UploadImageDTO (
        Long uploadId,
        String fileName,
        Integer width,
        Integer height,
        String format,
        LocalDateTime createdAt
){
}

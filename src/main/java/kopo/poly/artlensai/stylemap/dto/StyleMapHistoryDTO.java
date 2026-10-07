package kopo.poly.artlensai.stylemap.dto;

import kopo.poly.artlensai.analysis.dto.StyleMapDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record StyleMapHistoryDTO(

        String id,

        Long historyId,

        String title,

        String thumbnail,

        String mainTag,

        LocalDateTime createdAt,

        StyleMapDTO styleMap,

        List<?> top5
) {
}

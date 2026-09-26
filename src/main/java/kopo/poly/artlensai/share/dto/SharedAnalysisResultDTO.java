package kopo.poly.artlensai.share.dto;

import kopo.poly.artlensai.analysis.dto.ArtistResultDTO;
import kopo.poly.artlensai.analysis.dto.PaletteDTO;
import kopo.poly.artlensai.analysis.dto.StyleMapDTO;
import kopo.poly.artlensai.analysis.dto.TagResultDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공개 공유 페이지용 축소 DTO. userId/historyId/uploadId 같은 내부 식별자는 내려주지 않는다.
 */
@Builder
public record SharedAnalysisResultDTO(

        String imgKey,

        String title,

        String comment,

        List<TagResultDTO> tags,

        List<PaletteDTO> palette,

        List<ArtistResultDTO> top5,

        StyleMapDTO styleMap,

        LocalDateTime createdAt

) {
}

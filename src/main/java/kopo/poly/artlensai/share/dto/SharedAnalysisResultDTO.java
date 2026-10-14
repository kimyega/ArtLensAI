package kopo.poly.artlensai.share.dto;

import kopo.poly.artlensai.analysis.dto.ArtistResultDTO;
import kopo.poly.artlensai.analysis.dto.PaletteDTO;
import kopo.poly.artlensai.analysis.dto.StyleMapDTO;
import kopo.poly.artlensai.analysis.dto.TagResultDTO;
import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
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

        /**
         * MongoDB로 전달받은 document 결과를 DTO로 반환하기
         */
        public static SharedAnalysisResultDTO from(AnalysisResultsDocument doc) {

                return SharedAnalysisResultDTO.builder()
                        .imgKey(doc.getImgKey())
                        .title(doc.getTitle())
                        .comment(doc.getComment())
                        .tags(doc.getTags())
                        .palette(doc.getPalette())
                        .top5(doc.getTop5())
                        .styleMap(doc.getStyleMap())
                        .createdAt(doc.getCreatedAt())
                        .build();
        }
}

package kopo.poly.artlensai.analysis.repository.mongo.document;

import kopo.poly.artlensai.analysis.dto.ArtistResultDTO;
import kopo.poly.artlensai.analysis.dto.PaletteDTO;
import kopo.poly.artlensai.analysis.dto.StyleMapDTO;
import kopo.poly.artlensai.analysis.dto.TagResultDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "analysis_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultsDocument {

    @Id
    private String id;

    private Long historyId;

    private Long userId;

    private Long uploadId;

    private String imgKey;

    private String title;

    private String comment;

    private List<TagResultDTO> tags;

    private List<PaletteDTO> palette;

    private List<ArtistResultDTO> top5;

    private StyleMapDTO styleMap;

    private LocalDateTime createdAt;
}
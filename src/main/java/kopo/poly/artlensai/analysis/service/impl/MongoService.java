package kopo.poly.artlensai.analysis.service.impl;

import kopo.poly.artlensai.analysis.domain.postgres.entity.AnalysisHistoryEntity;
import kopo.poly.artlensai.analysis.domain.postgres.entity.UploadImageEntity;
import kopo.poly.artlensai.analysis.dto.AnalysisResultDTO;
import kopo.poly.artlensai.analysis.repository.mongo.AnalysisResultsRepository;
import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoService {

    private final AnalysisResultsRepository repository;

    public String saveAnalysisResult(

            AnalysisHistoryEntity history,

            UploadImageEntity image,

            AnalysisResultDTO body

    ) {

        AnalysisResultsDocument doc =

                AnalysisResultsDocument.builder()

                        .historyId(
                                history.getHistoryId()
                        )

                        .userId(
                                history.getUserId()
                        )

                        .uploadId(
                                history.getUploadId()
                        )

                        .imgKey(
                                image.getImgKey()
                        )

                        .title(
                                body.title()
                        )

                        .comment(
                                body.comment()
                        )

                        .tags(
                                body.tags()
                        )

                        .palette(
                                body.palette()
                        )

                        .top5(
                                body.top5()
                        )

                        .styleMap(
                                body.styleMap()
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();

        AnalysisResultsDocument saved =
                repository.save(doc);

        log.info(
                "🔥 MONGO SAVE ID={}",
                saved.getId()
        );

        return saved.getId();
    }
}

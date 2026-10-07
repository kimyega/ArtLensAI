package kopo.poly.artlensai.history.service.impl;

import kopo.poly.artlensai.analysis.repository.mongo.AnalysisResultsRepository;
import kopo.poly.artlensai.analysis.repository.postgres.AnalysisHistoryRepository;
import kopo.poly.artlensai.history.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService implements IHistoryService {

    private final AnalysisHistoryRepository analysisHistoryRepository;

    private final AnalysisResultsRepository analysisResultsRepository;

    @Override
    @Transactional
    public void deleteHistory(

            List<Long> ids

    ) throws Exception {

        log.info(
                "delete history ids={}",
                ids
        );

        analysisResultsRepository
                .deleteByHistoryIdIn(ids);

        log.info(
                "mongo delete complete"
        );

        analysisHistoryRepository
                .deleteAllById(ids);

        log.info(
                "postgres delete complete"
        );
    }
}

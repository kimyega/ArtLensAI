package kopo.poly.artlensai.analysis.repository.mongo;

import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisResultsRepository
        extends MongoRepository<
                AnalysisResultsDocument,
                String
                > {

    void deleteByHistoryIdIn(List<Long> historyIds);

    Long countByUserId(Long userId);

    List<AnalysisResultsDocument> findByUserId(Long userId);
}

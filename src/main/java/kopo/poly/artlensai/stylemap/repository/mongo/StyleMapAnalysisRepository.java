package kopo.poly.artlensai.stylemap.repository.mongo;

import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StyleMapAnalysisRepository
        extends MongoRepository<
                AnalysisResultsDocument,
                String
                > {

    List<AnalysisResultsDocument>
    findByUserIdOrderByCreatedAtDesc(
            Long userId
    );
}

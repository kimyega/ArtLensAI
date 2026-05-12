package kopo.poly.artlensai.analysis.repository.postgres;

import kopo.poly.artlensai.analysis.domain.postgres.entity.AnalysisHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistoryEntity, Long> {
}

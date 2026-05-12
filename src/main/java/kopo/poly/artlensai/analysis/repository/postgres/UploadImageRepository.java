package kopo.poly.artlensai.analysis.repository.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadImageRepository extends JpaRepository<kopo.poly.artlensai.analysis.domain.postgres.entity.UploadImageEntity, Long> {
}

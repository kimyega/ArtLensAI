package kopo.poly.artlensai.tastetest.domain.postgres;

import kopo.poly.artlensai.tastetest.domain.postgres.entity.TasteTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasteTestRepository
        extends JpaRepository<TasteTestEntity, Long> {
}

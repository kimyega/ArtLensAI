package kopo.poly.artlensai.recommend.domain.postgres;

import kopo.poly.artlensai.recommend.domain.postgres.entity.UserTasteEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTasteEmbeddingRepository
        extends JpaRepository<UserTasteEmbeddingEntity, Long> {

    Optional<UserTasteEmbeddingEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}

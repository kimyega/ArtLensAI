package kopo.poly.artlensai.share.domain.postgres;

import kopo.poly.artlensai.share.domain.postgres.entity.ShareLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareLinkRepository
        extends JpaRepository<ShareLinkEntity, Long> {

    Optional<ShareLinkEntity> findByToken(String token);

    Optional<ShareLinkEntity> findByResultIdAndUserIdAndIsPublicTrue(String resultId, Long userId);

    Optional<ShareLinkEntity> findByShareIdAndUserId(Long shareId, Long userId);

    List<ShareLinkEntity> findByResultIdInAndUserIdAndIsPublicTrue(List<String> resultIds, Long userId);
}

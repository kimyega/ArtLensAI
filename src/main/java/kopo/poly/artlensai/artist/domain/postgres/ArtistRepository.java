package kopo.poly.artlensai.artist.domain.postgres;


import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRepository
        extends JpaRepository<ArtistEntity, Long> {

    List<ArtistEntity> findTop30ByOrderByPostCntDesc();
}

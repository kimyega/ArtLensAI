package kopo.poly.artlensai.stylemap.repository.mongo;

import kopo.poly.artlensai.stylemap.domain.postgres.entity.ArtistCoordEntity;
import kopo.poly.artlensai.stylemap.dto.StyleMapArtistDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistEntity;
import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistImageEntity;

import java.util.List;

@Repository
public interface ArtistCoordRepository
        extends JpaRepository<
                ArtistCoordEntity,
                Long
                > {

}

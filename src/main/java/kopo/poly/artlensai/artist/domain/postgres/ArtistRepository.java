package kopo.poly.artlensai.artist.domain.postgres;


import kopo.poly.artlensai.artist.domain.postgres.entity.ArtistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository
        extends JpaRepository<ArtistEntity, Long> {
}

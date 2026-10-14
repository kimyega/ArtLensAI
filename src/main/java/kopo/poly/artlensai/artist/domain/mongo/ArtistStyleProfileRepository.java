package kopo.poly.artlensai.artist.domain.mongo;

// ArtistStyleProfileRepository.java

import kopo.poly.artlensai.artist.domain.mongo.document.ArtistStyleProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ArtistStyleProfileRepository
        extends MongoRepository<
        ArtistStyleProfileDocument,
        String
        > {

    Optional<ArtistStyleProfileDocument>
    findByArtistId(Integer artistId);
}

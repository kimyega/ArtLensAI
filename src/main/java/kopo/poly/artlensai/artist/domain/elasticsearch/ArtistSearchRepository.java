// ArtistSearchRepository.java

package kopo.poly.artlensai.artist.domain.elasticsearch;

import kopo.poly.artlensai.artist.domain.elasticsearch.document.ArtistSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArtistSearchRepository
        extends ElasticsearchRepository<ArtistSearchDocument, String> {
}

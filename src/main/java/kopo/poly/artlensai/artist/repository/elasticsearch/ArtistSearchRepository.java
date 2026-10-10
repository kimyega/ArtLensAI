// ArtistSearchRepository.java

package kopo.poly.artlensai.artist.repository.elasticsearch;

import kopo.poly.artlensai.artist.repository.elasticsearch.document.ArtistSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArtistSearchRepository
        extends ElasticsearchRepository<ArtistSearchDocument, String> {
}

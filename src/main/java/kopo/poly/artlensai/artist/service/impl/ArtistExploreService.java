// ArtistExploreService.java

package kopo.poly.artlensai.artist.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import kopo.poly.artlensai.artist.dto.ArtistExploreDTO;
import kopo.poly.artlensai.artist.dto.ArtistExploreSearchDTO;
import kopo.poly.artlensai.artist.repository.elasticsearch.document.ArtistSearchDocument;
import kopo.poly.artlensai.artist.service.IArtistExploreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistExploreService
        implements IArtistExploreService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<ArtistExploreDTO>
    getArtistExploreList(
            ArtistExploreSearchDTO pDTO
    ) {

        log.info(
                this.getClass().getName()
                        + ".getArtistExploreList Start!"
        );

        Query query =
                Query.of(q -> q.bool(b -> {

                    addTagFilter(b, pDTO.style());
                    addTagFilter(b, pDTO.color());
                    addTagFilter(b, pDTO.lighting());
                    addTagFilter(b, pDTO.composition());
                    addTagFilter(b, pDTO.mood());

                    addArtistNameFilter(b, pDTO.artistName());
                    addArtistIdFilter(b, pDTO.artistIds());

                    return b;
                }));

        NativeQuery nativeQuery =
                NativeQuery.builder()
                        .withQuery(query)
                        .withMaxResults(200)
                        .build();

        List<ArtistSearchDocument> docs =
                elasticsearchOperations
                        .search(
                                nativeQuery,
                                ArtistSearchDocument.class
                        )
                        .stream()
                        .map(SearchHit::getContent)
                        .toList();

        return docs.stream()

                .map(doc ->

                        ArtistExploreDTO
                                .builder()

                                .artistId(
                                        doc.artistId()
                                )

                                .artistName(
                                        doc.artistName()
                                )

                                .profileImg(
                                        doc.profileImg()
                                )

                                .styleDesc(
                                        doc.styleDesc()
                                )

                                .tags(
                                        doc.tags()
                                )

                                .colors(
                                        doc.colors()
                                )

                                .build()

                )

                .toList();
    }

    private void addTagFilter(
            BoolQuery.Builder b,
            List<String> filters
    ) {

        if (filters == null ||
                filters.isEmpty()) {

            return;
        }

        b.must(m -> m.terms(t -> t
                .field("tags")
                .terms(tv -> tv.value(
                        filters.stream()
                                .map(FieldValue::of)
                                .toList()
                ))
        ));
    }

    private void addArtistNameFilter(
            BoolQuery.Builder b,
            List<String> artistNames
    ) {

        if (artistNames == null ||
                artistNames.isEmpty()) {

            return;
        }

        b.must(m -> m.terms(t -> t
                .field("artistName.raw")
                .terms(tv -> tv.value(
                        artistNames.stream()
                                .map(FieldValue::of)
                                .toList()
                ))
        ));
    }

    private void addArtistIdFilter(
            BoolQuery.Builder b,
            List<Long> artistIds
    ) {

        if (artistIds == null ||
                artistIds.isEmpty()) {

            return;
        }

        b.must(m -> m.terms(t -> t
                .field("artistId")
                .terms(tv -> tv.value(
                        artistIds.stream()
                                .map(FieldValue::of)
                                .toList()
                ))
        ));
    }

    // =========================
    // 작가 이름 자동완성
    // =========================
    @Override
    public List<String> suggestArtistNames(String keyword) {

        if (keyword == null || keyword.isBlank()) {

            return List.of();
        }

        String lower = keyword.toLowerCase();

        String pattern = "*" + lower + "*";

        Query query =
                Query.of(q -> q.wildcard(w -> w
                        .field("artistName.raw")
                        .value(pattern)
                        .caseInsensitive(true)
                ));

        NativeQuery nativeQuery =
                NativeQuery.builder()
                        .withQuery(query)
                        .withMaxResults(30)
                        .build();

        return elasticsearchOperations
                .search(nativeQuery, ArtistSearchDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .map(ArtistSearchDocument::artistName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted((a, bName) -> {

                    boolean aStarts = a.toLowerCase().startsWith(lower);
                    boolean bStarts = bName.toLowerCase().startsWith(lower);

                    if (aStarts == bStarts) {
                        return 0;
                    }

                    return aStarts ? -1 : 1;
                })
                .limit(8)
                .toList();
    }
}

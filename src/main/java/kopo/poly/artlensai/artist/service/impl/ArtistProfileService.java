package kopo.poly.artlensai.artist.service.impl;

import kopo.poly.artlensai.analysis.dto.ArtistStyleProfileDTO;

import kopo.poly.artlensai.artist.domain.elasticsearch.ArtistSearchRepository;
import kopo.poly.artlensai.artist.domain.elasticsearch.document.ArtistSearchDocument;
import kopo.poly.artlensai.artist.domain.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.artist.domain.mongo.document.ArtistStyleProfileDocument;
import kopo.poly.artlensai.artist.service.IArtistProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistProfileService implements IArtistProfileService {

    private final ArtistStyleProfileRepository repository;

    private final ArtistSearchRepository artistSearchRepository;

    @Override
    public void saveProfile(
            ArtistStyleProfileDTO pDTO
    ) {

        ArtistStyleProfileDocument doc =

                ArtistStyleProfileDocument.builder()
                        .artistId(pDTO.artistId())
                        .artistName(pDTO.artistName())
                        .styleDesc(pDTO.styleDesc())
                        .colors(pDTO.colors())
                        .tags(pDTO.tags())
                        .profileImg(pDTO.profileImg())
                        .updatedAt(LocalDateTime.now())
                        .build();

        ArtistStyleProfileDocument saved =
                repository.save(doc);

        try {

            artistSearchRepository.save(
                    toSearchDocument(saved)
            );

        } catch (Exception e) {

            // MongoDB 저장은 이미 성공한 뒤라, ES 색인 실패가 전체 저장 실패처럼 보이면 안 됨.
            // 검색 인덱스만 일시적으로 어긋난 상태이므로 reindexAll()로 복구 가능.
            log.error(
                    "ES 색인 실패(MongoDB는 저장됨, 검색에서만 누락): artistId={}",
                    pDTO.artistId(),
                    e
            );
        }

        log.info(
                "Artist profile saved : {}",
                pDTO.artistName()
        );
    }

    @Override
    public void reindexAll() {

        List<ArtistStyleProfileDocument> profiles =
                repository.findAll();

        List<ArtistSearchDocument> searchDocs =
                profiles.stream()
                        .map(this::toSearchDocument)
                        .toList();

        artistSearchRepository.saveAll(searchDocs);

        log.info(
                "Artist search reindex done. count={}",
                searchDocs.size()
        );
    }

    private ArtistSearchDocument toSearchDocument(
            ArtistStyleProfileDocument doc
    ) {

        return ArtistSearchDocument.builder()
                .id(doc.id())
                .artistId(doc.artistId())
                .artistName(doc.artistName())
                .profileImg(doc.profileImg())
                .styleDesc(doc.styleDesc())
                .tags(doc.tags())
                .colors(doc.colors())
                .build();
    }
}

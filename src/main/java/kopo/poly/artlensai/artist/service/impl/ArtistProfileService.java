package kopo.poly.artlensai.artist.service.impl;

import kopo.poly.artlensai.analysis.dto.ArtistStyleProfileDTO;

import kopo.poly.artlensai.artist.repository.elasticsearch.ArtistSearchRepository;
import kopo.poly.artlensai.artist.repository.elasticsearch.document.ArtistSearchDocument;
import kopo.poly.artlensai.artist.repository.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.artist.repository.mongo.document.ArtistStyleProfileDocument;
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

        artistSearchRepository.save(
                toSearchDocument(saved)
        );

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

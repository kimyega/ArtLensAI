package kopo.poly.artlensai.artist.service.impl;

import kopo.poly.artlensai.analysis.dto.ArtistStyleProfileDTO;

import kopo.poly.artlensai.artist.repository.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.artist.repository.mongo.document.ArtistStyleProfileDocument;
import kopo.poly.artlensai.artist.service.IArtistProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistProfileService implements IArtistProfileService {

    private final ArtistStyleProfileRepository repository;

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

        repository.save(doc);

        log.info(
                "Artist profile saved : {}",
                pDTO.artistName()
        );
    }


}

package kopo.poly.artlensai.stylemap.service.impl;

import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import kopo.poly.artlensai.artist.domain.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.artist.domain.mongo.document.ArtistStyleProfileDocument;
import kopo.poly.artlensai.stylemap.domain.postgres.entity.ArtistCoordEntity;
import kopo.poly.artlensai.stylemap.dto.ArtistProfileDTO;
import kopo.poly.artlensai.stylemap.dto.StyleMapArtistDTO;
import kopo.poly.artlensai.stylemap.dto.StyleMapHistoryDTO;
import kopo.poly.artlensai.stylemap.repository.mongo.ArtistCoordRepository;
import kopo.poly.artlensai.stylemap.repository.mongo.StyleMapAnalysisRepository;
import kopo.poly.artlensai.stylemap.service.IStyleMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StyleMapService
        implements IStyleMapService {

    private final
    StyleMapAnalysisRepository repository;

    private final ArtistCoordRepository artistCoordRepository;

    private final ArtistStyleProfileRepository
            artistStyleProfileRepository;


    @Override
    public List<StyleMapHistoryDTO>
    getHistory(
            Long userId
    ) {

        return repository
                .findByUserIdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toStyleMapHistoryDTO)
                .toList();
    }

    @Override
    public List<StyleMapArtistDTO>
    getArtists() {

        return artistCoordRepository
                .findAll()
                .stream()
                .map(this::toStyleMapArtistDTO)
                .toList();
    }

    @Override
    public List<ArtistProfileDTO>
    getArtistProfiles() {

        return artistStyleProfileRepository
                .findAll()
                .stream()
                .map(this::toArtistProfileDTO)
                .toList();
    }

    private ArtistProfileDTO
    toArtistProfileDTO(
            ArtistStyleProfileDocument doc
    ) {

        return ArtistProfileDTO.builder()
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
                .build();
    }

    private StyleMapArtistDTO
    toStyleMapArtistDTO(
            ArtistCoordEntity entity
    ) {

        return StyleMapArtistDTO
                .builder()
                .artistId(
                        entity.getArtistId()
                )
                .x(
                        entity.getX()
                )
                .y(
                        entity.getY()
                )
                .clusterLabel(
                        entity.getClusterLabel()
                )
                .build();
    }

    private StyleMapHistoryDTO toStyleMapHistoryDTO(
            AnalysisResultsDocument doc
    ) {

        return StyleMapHistoryDTO
                .builder()
                .id(
                        doc.getId()
                )
                .historyId(
                        doc.getHistoryId()
                )
                .title(
                        doc.getTitle()
                )
                .thumbnail(
                        doc.getImgKey()
                )
                .mainTag(
                        "#"
                                + doc.getTags()
                                .get(0)
                                .tag()
                )
                .createdAt(
                        doc.getCreatedAt()
                )
                .styleMap(
                        doc.getStyleMap()
                )
                .top5(
                        doc.getTop5()
                )
                .build();
    }
}

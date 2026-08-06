// ArtistExploreService.java

package kopo.poly.artlensai.artist.service.impl;

import kopo.poly.artlensai.artist.dto.ArtistExploreDTO;
import kopo.poly.artlensai.artist.dto.ArtistExploreSearchDTO;
import kopo.poly.artlensai.artist.repository.mongo.ArtistStyleProfileRepository;
import kopo.poly.artlensai.artist.repository.mongo.document.ArtistStyleProfileDocument;
import kopo.poly.artlensai.artist.service.IArtistExploreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistExploreService
        implements IArtistExploreService {

    private final ArtistStyleProfileRepository artistStyleProfileRepository;

    @Override
    public List<ArtistExploreDTO>
    getArtistExploreList(
            ArtistExploreSearchDTO pDTO
    ) {

        log.info(
                this.getClass().getName()
                        + ".getArtistExploreList Start!"
        );

        List<ArtistStyleProfileDocument>
                profileList;

        if (pDTO.keyword() != null &&
                !pDTO.keyword().isBlank()) {

            String keyword =
                    pDTO.keyword()
                            .toLowerCase();

            profileList =
                    artistStyleProfileRepository
                            .findAll()

                            .stream()

                            .filter(profile ->

                                    profile.artistName()
                                            .toLowerCase()
                                            .contains(keyword)

                                            ||

                                            profile.tags()
                                                    .stream()

                                                    .anyMatch(tag ->

                                                            tag.toLowerCase()
                                                                    .contains(keyword)
                                                    )
                            )

                            .toList();

        } else {

            profileList =
                    artistStyleProfileRepository
                            .findAll();
        }


        profileList = filterByTags(
                profileList,
                pDTO.style()
        );

        profileList = filterByTags(
                profileList,
                pDTO.color()
        );

        profileList = filterByTags(
                profileList,
                pDTO.lighting()
        );

        profileList = filterByTags(
                profileList,
                pDTO.composition()
        );

        profileList = filterByTags(
                profileList,
                pDTO.mood()
        );

        return profileList.stream()

                .map(profile ->

                        ArtistExploreDTO
                                .builder()

                                .artistId(
                                        profile.artistId()
                                )

                                .artistName(
                                        profile.artistName()
                                )

                                .profileImg(
                                        profile.profileImg()
                                )

                                .styleDesc(
                                        profile.styleDesc()
                                )

                                .tags(
                                        profile.tags()
                                )

                                .colors(
                                        profile.colors()
                                )

                                .build()

                )

                .toList();
    }

    private List<ArtistStyleProfileDocument>
    filterByTags(

            List<ArtistStyleProfileDocument>
                    profileList,

            List<String> filters
    ) {

        if (filters == null ||
                filters.isEmpty()) {

            return profileList;
        }

        return profileList.stream()

                .filter(profile ->

                        profile.tags()
                                .stream()

                                .anyMatch(filters::contains)
                )

                .toList();
    }
}
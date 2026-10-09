// IArtistExploreService.java

package kopo.poly.artlensai.artist.service;

import kopo.poly.artlensai.artist.dto.ArtistExploreDTO;
import kopo.poly.artlensai.artist.dto.ArtistExploreSearchDTO;

import java.util.List;

public interface IArtistExploreService {

    List<ArtistExploreDTO> getArtistExploreList(ArtistExploreSearchDTO pDTO);

    List<String> suggestArtistNames(String keyword);
}
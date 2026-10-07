package kopo.poly.artlensai.stylemap.service;

import kopo.poly.artlensai.stylemap.dto.ArtistProfileDTO;
import kopo.poly.artlensai.stylemap.dto.StyleMapArtistDTO;
import kopo.poly.artlensai.stylemap.dto.StyleMapHistoryDTO;

import java.util.List;

public interface IStyleMapService {

    List<StyleMapHistoryDTO>
    getHistory(
            Long userId
    );

    List<StyleMapArtistDTO>
    getArtists();

    List<ArtistProfileDTO> getArtistProfiles();
}

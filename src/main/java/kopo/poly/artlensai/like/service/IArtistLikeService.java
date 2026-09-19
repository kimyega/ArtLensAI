package kopo.poly.artlensai.like.service;

import kopo.poly.artlensai.like.dto.LikeStatusDTO;
import kopo.poly.artlensai.like.dto.LikeToggleResponseDTO;

public interface IArtistLikeService {

    LikeStatusDTO getLikedStatus(Long userId);

    LikeToggleResponseDTO like(Long userId, Long artistId);

    LikeToggleResponseDTO unlike(Long userId, Long artistId);
}

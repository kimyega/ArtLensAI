package kopo.poly.artlensai.share.service;

import kopo.poly.artlensai.share.dto.ShareLinkDTO;
import kopo.poly.artlensai.share.dto.SharedAnalysisResultDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IShareService {

    ShareLinkDTO createOrGetShare(Long userId, String resultId, Integer expiresInDays);

    Optional<ShareLinkDTO> getMyShare(Long userId, String resultId);

    Map<String, ShareLinkDTO> getMyShares(Long userId, List<String> resultIds);

    void revoke(Long userId, Long shareId);

    SharedAnalysisResultDTO getPublicResult(String token);
}

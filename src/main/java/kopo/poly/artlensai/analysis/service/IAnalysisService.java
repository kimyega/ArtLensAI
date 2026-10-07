package kopo.poly.artlensai.analysis.service;

import kopo.poly.artlensai.analysis.dto.AnalysisResultDTO;
import kopo.poly.artlensai.analysis.dto.AnalysisStartResponseDTO;
import kopo.poly.artlensai.user.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IAnalysisService {
    Long saveUploadImage(MultipartFile file, UserDTO pDTO);

    AnalysisStartResponseDTO startAnalysis(Long uploadId, UserDTO pDTO);

}

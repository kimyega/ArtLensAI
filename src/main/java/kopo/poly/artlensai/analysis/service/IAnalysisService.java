package kopo.poly.artlensai.analysis.service;

import kopo.poly.artlensai.user.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IAnalysisService {
    Long saveUploadImage(MultipartFile file, UserDTO pDTO);

    void startAnalysis(Long uploadId, UserDTO pDTO);

}

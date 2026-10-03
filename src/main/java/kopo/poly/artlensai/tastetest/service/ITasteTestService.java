package kopo.poly.artlensai.tastetest.service;

import kopo.poly.artlensai.tastetest.dto.TasteAnswerRequestDTO;
import kopo.poly.artlensai.tastetest.dto.TasteTestResponseDTO;

public interface ITasteTestService {

    TasteTestResponseDTO startTest(Long userId);

    TasteTestResponseDTO submitAnswer(Long testId, Long userId, TasteAnswerRequestDTO reqDTO);
}

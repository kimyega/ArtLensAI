package kopo.poly.artlensai.analysis.service;

import kopo.poly.artlensai.analysis.dto.AnalysisUsageDTO;

public interface IAnalysisUsageService {

    AnalysisUsageDTO getUsage(Long userId);

    int getRemaining(Long userId);

    /**
     * 오늘 분석 횟수를 1 소모한다. 한도를 넘으면 증가분을 롤백하고 예외를 던진다.
     */
    void consumeOrThrow(Long userId);
}

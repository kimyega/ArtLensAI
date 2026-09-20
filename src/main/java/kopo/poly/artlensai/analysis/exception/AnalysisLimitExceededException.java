package kopo.poly.artlensai.analysis.exception;

import kopo.poly.artlensai.analysis.dto.AnalysisUsageDTO;
import lombok.Getter;

@Getter
public class AnalysisLimitExceededException extends RuntimeException {

    private final AnalysisUsageDTO usage;

    public AnalysisLimitExceededException(AnalysisUsageDTO usage) {

        super("오늘의 분석 가능 횟수를 모두 사용했어요.");

        this.usage = usage;
    }
}

package kopo.poly.artlensai.tastetest.domain.postgres;

import kopo.poly.artlensai.tastetest.domain.postgres.entity.TasteTestAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TasteTestAnswerRepository
        extends JpaRepository<TasteTestAnswerEntity, Long> {

    List<TasteTestAnswerEntity> findByTestId(Long testId);

    @Query("SELECT DISTINCT a.imgId FROM TasteTestAnswerEntity a WHERE a.testId = :testId AND a.isPicked = true")
    List<Long> findDistinctPickedImgIds(@Param("testId") Long testId);

    @Query("SELECT COUNT(DISTINCT a.questionNo) FROM TasteTestAnswerEntity a WHERE a.testId = :testId")
    int countAnsweredQuestions(@Param("testId") Long testId);
}

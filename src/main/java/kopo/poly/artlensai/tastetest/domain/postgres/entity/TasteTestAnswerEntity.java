package kopo.poly.artlensai.tastetest.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "taste_test_answers")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasteTestAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "question_no")
    private Integer questionNo;

    @Column(name = "img_id")
    private Long imgId;

    @Column(name = "is_picked")
    private Boolean isPicked;
}

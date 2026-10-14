package kopo.poly.artlensai.recommend.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_taste_embeddings")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTasteEmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taste_id")
    private Long tasteId;

    @Column(name = "user_id")
    private Long userId;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(name = "embd_vect")
    private float[] embdVect;

    @Column(name = "like_cnt")
    private Integer likeCnt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateTaste(float[] embdVect, int likeCnt) {

        this.embdVect = embdVect;
        this.likeCnt = likeCnt;
        this.updatedAt = LocalDateTime.now();
    }
}

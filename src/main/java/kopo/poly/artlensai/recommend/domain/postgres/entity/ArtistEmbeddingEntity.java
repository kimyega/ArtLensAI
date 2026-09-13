package kopo.poly.artlensai.recommend.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "artist_embeddings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistEmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "embd_id")
    private Long embdId;

    @Column(name = "artist_id")
    private Long artistId;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(name = "embd_vect")
    private float[] embdVect;

    @Column(name = "img_cnt")
    private Integer imgCnt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

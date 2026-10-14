package kopo.poly.artlensai.recommend.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "artwork_embeddings")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtworkEmbeddingEntity {

    @Id
    @Column(name = "img_id")
    private Long imgId;

    @Column(name = "artist_id")
    private Long artistId;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(name = "embd_vect")
    private float[] embdVect;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

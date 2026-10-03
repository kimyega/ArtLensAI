package kopo.poly.artlensai.tastetest.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "taste_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasteTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long testId;

    @Column(name = "user_id")
    private Long userId;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    @Column(name = "taste_vect")
    private float[] tasteVect;

    @Column(name = "cluster_label")
    private String clusterLabel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

package kopo.poly.artlensai.stylemap.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "artist_coords")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistCoordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coord_id")
    private Long coordId;

    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    @Column(name = "cluster_label")
    private String clusterLabel;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
